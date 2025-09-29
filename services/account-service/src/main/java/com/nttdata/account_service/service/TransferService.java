package com.nttdata.account_service.service;

import com.nttdata.account_service.domain.Account;
import com.nttdata.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepo;
    private final TransactionService txService; // registra movimientos

    public Mono<Void> transferSameCustomer(String fromId, String toId, BigDecimal amount) {
        return Mono.zip(accountRepo.findById(fromId), accountRepo.findById(toId))
                .flatMap(tuple -> {
                    Account from = tuple.getT1(); Account to = tuple.getT2();
                    if (!from.getCustomerId().equals(to.getCustomerId())) {
                        return Mono.error(new IllegalArgumentException("Accounts belong to different customers"));
                    }
                    if (from.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new IllegalStateException("Insufficient funds"));
                    }
                    from.setBalance(from.getBalance().subtract(amount));
                    to.setBalance(to.getBalance().add(amount));
                    return accountRepo.save(from)
                            .then(accountRepo.save(to))
                            .then(txService.recordTransfer(from.getId(), to.getId(), amount))
                            .then();
                });
    }

    public Mono<Void> transferThirdParty(String fromId, String toId, BigDecimal amount) {
        return Mono.zip(accountRepo.findById(fromId), accountRepo.findById(toId))
                .flatMap(tuple -> {
                    Account from = tuple.getT1(); Account to = tuple.getT2();
                    if (from.getCustomerId().equals(to.getCustomerId())) {
                        return Mono.error(new IllegalArgumentException("Use /own for same customer"));
                    }
                    if (from.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new IllegalStateException("Insufficient funds"));
                    }
                    from.setBalance(from.getBalance().subtract(amount));
                    to.setBalance(to.getBalance().add(amount));
                    return accountRepo.save(from)
                            .then(accountRepo.save(to))
                            .then(txService.recordTransfer(from.getId(), to.getId(), amount))
                            .then();
                });
    }
}