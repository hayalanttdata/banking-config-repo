package com.nttdata.transaction_service.application;

import com.nttdata.transaction_service.domain.Transaction;
import com.nttdata.transaction_service.dto.CommissionReportDto;
import com.nttdata.transaction_service.dto.DailyBalanceReportDto;
import com.nttdata.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Report use cases:
 *  - Daily average balance of all client products for current month.
 *  - Total commissions grouped by product in a date range.
 *
 * Nota: Para reconstruir saldos diarios se acumulan los cambios del día.
 *      Si quieres mayor exactitud, trae el balance inicial del producto
 *      desde account-service y úsalo como "carry" inicial.
 */
@Service
@RequiredArgsConstructor
public class ReportAppService {

    private static final MathContext MC = MathContext.DECIMAL64;

    private final TransactionRepository txRepo;

    /**
     * Daily average balance for the current month for all products of a client.
     * @param customerId customer id
     * @return One item per product with its average
     */
    public Flux<DailyBalanceReportDto> generateDailyBalanceReport(String customerId) {
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = LocalDate.now(); // hasta hoy

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay().minusNanos(1);

        return txRepo.findByCustomerIdAndOccurredAtBetween(customerId, from, to)
                .collectList()
                .flatMapMany(list -> Flux.fromIterable(computeDailyAverages(list, start, end)));
    }

    /**
     * Commissions grouped by product in a date range.
     * @param from inclusive
     * @param to   inclusive
     */
    public Flux<CommissionReportDto> generateCommissionReport(LocalDate from, LocalDate to) {
        LocalDateTime f = from.atStartOfDay();
        LocalDateTime t = to.plusDays(1).atStartOfDay().minusNanos(1);

        return txRepo.findByOccurredAtBetweenAndCommissionGreaterThan(f, t, BigDecimal.ZERO)
                .collectList()
                .flatMapMany(list -> {
                    Map<String, List<Transaction>> byProduct = list.stream()
                            .collect(Collectors.groupingBy(Transaction::getProductId));
                    List<CommissionReportDto> out = byProduct.entrySet().stream()
                            .map(e -> {
                                String productId = e.getKey();
                                String productType = e.getValue().stream()
                                        .map(Transaction::getProductType)
                                        .filter(s -> s != null && !s.isBlank())
                                        .findFirst().orElse("UNKNOWN");
                                BigDecimal total = e.getValue().stream()
                                        .map(Transaction::getCommission)
                                        .filter(c -> c != null)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                return new CommissionReportDto(productId, productType, total);
                            })
                            .toList();
                    return Flux.fromIterable(out);
                });
    }

    /* ======================= Helpers ======================= */

    private List<DailyBalanceReportDto> computeDailyAverages(List<Transaction> txs,
                                                             LocalDate start, LocalDate end) {
        // Group by product
        Map<String, List<Transaction>> byProduct = txs.stream()
                .collect(Collectors.groupingBy(Transaction::getProductId));

        return byProduct.entrySet().stream()
                .map(entry -> {
                    String productId = entry.getKey();
                    List<Transaction> list = entry.getValue().stream()
                            .sorted(Comparator.comparing(Transaction::getOccurredAt))
                            .toList();

                    String productType = list.stream()
                            .map(Transaction::getProductType)
                            .filter(s -> s != null && !s.isBlank())
                            .findFirst().orElse("UNKNOWN");

                    // Net change per day
                    Map<LocalDate, BigDecimal> changesPerDay = list.stream()
                            .collect(Collectors.groupingBy(
                                    t -> t.getOccurredAt().toLocalDate(),
                                    Collectors.reducing(
                                            BigDecimal.ZERO,
                                            this::signedAmount,
                                            BigDecimal::add)));

                    // Rebuild end-of-day balances assuming starting balance = 0 (ver nota arriba)
                    BigDecimal running = BigDecimal.ZERO;
                    BigDecimal sumEod = BigDecimal.ZERO;
                    int daysCount = 0;

                    LocalDate cursor = start;
                    while (!cursor.isAfter(end)) {
                        BigDecimal delta = changesPerDay.getOrDefault(cursor, BigDecimal.ZERO);
                        running = running.add(delta, MC); // end of day balance
                        sumEod = sumEod.add(running, MC);
                        daysCount++;
                        cursor = cursor.plusDays(1);
                    }

                    BigDecimal avg = daysCount == 0 ? BigDecimal.ZERO
                            : sumEod.divide(BigDecimal.valueOf(daysCount), MC);

                    return new DailyBalanceReportDto(productId, productType, avg);
                })
                .toList();
    }

    private BigDecimal signedAmount(Transaction t) {
        if (t.getAmount() == null) return BigDecimal.ZERO;
        // Transfers/withdrawals disminuyen, deposits aumentan; fee disminuye balance
        return switch (t.getType()) {
            case DEPOSIT, TRANSFER_IN -> t.getAmount();
            case WITHDRAW, TRANSFER_OUT, FEE -> t.getAmount().negate();
        };
    }
}