package com.nttdata.account_service.service;

import com.nttdata.account_service.domain.Account;
import com.nttdata.account_service.domain.AccountType;
import com.nttdata.account_service.repository.AccountRepository;
import com.nttdata.account_service.service.AccountDomainService;
import com.nttdata.account_service.service.TransactionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Application service for Accounts.
 * - Exposes CRUD use cases
 * - Domain rules: min opening amount, VIP/PYME requirements (delegated to AccountDomainService)
 * - Deposit/Withdraw with free-transaction limit and per-transaction fee after the limit
 * - Records movements via TransactionClient (transaction-service)
 *
 * Reactive, side-effect free (except persistence) and composable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountAppService {

    private final AccountRepository accountRepository;
    private final AccountDomainService domain;     // reglas de negocio parametrizadas (Parte II)
    private final TransactionClient txClient;      // adapter WebClient hacia transaction-service

    /* ---------------------------- CRUD ---------------------------- */

    public Flux<Account> findAll() {
        return accountRepository.findAll();
    }

    public Mono<Account> findById(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Account not found: " + id)));
    }

    public Mono<Account> create(Account input) {
        // reglas de dominio (mínimos, VIP/PYME, etc.)
        return domain.validateCreation(input)
                .flatMap((Account -> accountRepository.save(Acount))
                //.flatMap(accountRepository::save)
                .doOnSuccess(a -> log.info("Account created: {}", a.getId()));

    }

    public Mono<Account> update(String id, Account patch) {
        return findById(id)
                .flatMap(existing -> {
                    // Campos permitidos a actualizar (ajusta según tu modelo)
                    if (patch.getAccountType() != null) existing.setAccountType(patch.getAccountType());
                    if (patch.getMaintenanceFee() != null) existing.setMaintenanceFee(patch.getMaintenanceFee());
                    if (patch.getMonthlyMovementLimit() != null) existing.setMonthlyMovementLimit(patch.getMonthlyMovementLimit());
                    // Por regla general, el balance no se parchea por update sino por operaciones (dep/with)
                    return accountRepository.save(existing);
                })
                .doOnSuccess(a -> log.info("Account updated: {}", a.getId()));
    }

    public Mono<Void> delete(String id) {
        return accountRepository.existsById(id)
                .flatMap(exists -> exists ? accountRepository.deleteById(id) : Mono.empty())
                .doOnSuccess(v -> log.info("Account deleted: {}", id));
    }

    /* ----------------------- Movimientos -------------------------- */

    public Mono<Account> deposit(String accountId, BigDecimal amount) {
        validateAmountPositive(amount, "amount must be > 0");

        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Account not found: " + accountId)))
                .flatMap(acc ->
                        shouldChargeFeeThisMonth(acc)
                                .flatMap(chargeFee -> {
                                    BigDecimal fee = chargeFee ? domain.fee(acc.getAccountType()) : BigDecimal.ZERO;
                                    BigDecimal newBalance = acc.getBalance().add(amount).subtract(fee);
                                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                                        return Mono.error(new IllegalStateException("Resulting balance cannot be negative"));
                                    }
                                    acc.setBalance(newBalance);
                                    return accountRepository.save(acc)
                                            .then(txClient.recordDeposit(acc.getId(), amount, fee))
                                            .thenReturn(acc);
                                })
                )
                .doOnSuccess(a -> log.info("Deposit ok. account={}, balance={}", a.getId(), a.getBalance()));
    }

    public Mono<Account> withdraw(String accountId, BigDecimal amount) {
        validateAmountPositive(amount, "amount must be > 0");

        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Account not found: " + accountId)))
                .flatMap(acc ->
                        shouldChargeFeeThisMonth(acc)
                                .flatMap(chargeFee -> {
                                    BigDecimal fee = chargeFee ? domain.fee(acc.getAccountType()) : BigDecimal.ZERO;
                                    BigDecimal totalDebit = amount.add(fee);
                                    if (acc.getBalance().compareTo(totalDebit) < 0) {
                                        return Mono.error(new IllegalStateException("Insufficient funds"));
                                    }
                                    acc.setBalance(acc.getBalance().subtract(totalDebit));
                                    return accountRepository.save(acc)
                                            .then(txClient.recordWithdraw(acc.getId(), amount, fee))
                                            .thenReturn(acc);
                                })
                )
                .doOnSuccess(a -> log.info("Withdraw ok. account={}, balance={}", a.getId(), a.getBalance()));
    }

    /* ----------------------- Helpers ------------------------------ */

    /**
     * Decide si se cobra comisión según # de transacciones del mes en curso y el límite free por tipo de cuenta.
     */
    private Mono<Boolean> shouldChargeFeeThisMonth(Account acc) {
        AccountType type = Objects.requireNonNull(acc.getAccountType(), "accountType is required");
        int freeAllowed = domain.freeTransactions(type);

        YearMonth ym = YearMonth.now();
        return txClient.countAccountTransactionsThisMonth(acc.getId(), ym)
                .map(count -> count >= freeAllowed);
    }

    private static void validateAmountPositive(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}