package com.nttdata.account_service.service;

import com.nttdata.account_service.model.BankAccount;
import com.nttdata.account_service.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service @RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository repository;

    public Flux<BankAccount> findAll() { return repository.findAll(); }
    public Mono<BankAccount> findById(String id) { return repository.findById(id); }
    public Mono<BankAccount> create(BankAccount a) {
        if (a.getBalance() == null) a.setBalance(BigDecimal.ZERO);
        return repository.save(a);
    }
    public Mono<BankAccount> update(String id, BankAccount dto) {
        return repository.findById(id).flatMap(db -> {
            db.setType(dto.getType());
            db.setMaintenanceFee(dto.getMaintenanceFee());
            db.setMonthlyMovementLimit(dto.getMonthlyMovementLimit());
            return repository.save(db);
        });
    }
    public Mono<Void> delete(String id) { return repository.deleteById(id); }

    public Mono<BankAccount> deposit(String id, BigDecimal amount) {
        return repository.findById(id)
                .flatMap(acc -> {
                    acc.setBalance(acc.getBalance().add(amount));
                    return repository.save(acc);
                });
    }

    public Mono<BankAccount> withdraw(String id, BigDecimal amount) {
        return repository.findById(id)
                .flatMap(acc -> {
                    if (acc.getBalance().compareTo(amount) < 0) return Mono.error(new IllegalStateException("Insufficient funds"));
                    acc.setBalance(acc.getBalance().subtract(amount));
                    return repository.save(acc);
                });
    }
}