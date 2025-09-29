package com.nttdata.account_service.service;

import com.nttdata.account_service.domain.Account;
import com.nttdata.account_service.domain.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountDomainService {

    @Value("#{${rules.opening.minimum}}")
    private Map<AccountType, BigDecimal> minOpeningByType;

    @Value("#{${rules.freeTransactions}}")
    private Map<AccountType, Integer> freeTxByType;

    @Value("#{${rules.transactionFee}}")
    private Map<AccountType, BigDecimal> feeByType;

    @Value("${rules.vip.minDailyAvg}")
    private BigDecimal vipMinDailyAvg;

    @Value("${rules.pyme.maintenanceFee}")
    private BigDecimal pymeMaintenanceFee;

    private final ExternalCardClient cardClient;        // WebClient a card-service
    private final ExternalCustomerClient customerClient;// WebClient a customer-service
    private final BalanceService balanceService;        // cálculo promedio diario

    public Mono<Account> validateCreation(Account acc) {
        // 1) mínimo de apertura
        var min = minOpeningByType.getOrDefault(acc.getAccountType(), BigDecimal.ZERO);
        if (acc.getBalance().compareTo(min) < 0) {
            return Mono.error(new IllegalArgumentException("Opening balance must be >= " + min));
        }

        // 2) VIP / PYME requisitos (tener tarjeta de crédito)
        return customerClient.findById(acc.getCustomerId())
                .flatMap(c -> {
                    switch (c.getProfile()) {
                        case PERSONAL_VIP:
                            // Debe tener tarjeta y cumplir promedio mínimo mensual
                            return cardClient.hasAnyCard(c.getId())
                                    .filter(Boolean::booleanValue)
                                    .switchIfEmpty(Mono.error(new IllegalStateException("VIP requires at least one credit card")))
                                    .then(balanceService.monthToDateDailyAverage(c.getId(), acc.getId())
                                            .filter(avg -> avg.compareTo(vipMinDailyAvg) >= 0)
                                            .switchIfEmpty(Mono.error(new IllegalStateException("VIP requires min daily average: " + vipMinDailyAvg)))
                                            .thenReturn(acc));
                        case BUSINESS_PYME:
                            // Debe tener tarjeta; cuenta corriente sin mantenimiento
                            if (acc.getAccountType() != AccountType.CURRENT) {
                                return Mono.error(new IllegalArgumentException("PYME only allows CURRENT accounts"));
                            }
                            return cardClient.hasAnyCard(c.getId())
                                    .filter(Boolean::booleanValue)
                                    .switchIfEmpty(Mono.error(new IllegalStateException("PYME requires at least one credit card")))
                                    .then(Mono.fromCallable(() -> { acc.setMaintenanceFee(pymeMaintenanceFee); return acc; }));
                        default:
                            return Mono.just(acc);
                    }
                });
    }

    public int freeTransactions(AccountType type) {
        return freeTxByType.getOrDefault(type, 0);
    }

    public BigDecimal fee(AccountType type) {
        return feeByType.getOrDefault(type, BigDecimal.ZERO);
    }
}