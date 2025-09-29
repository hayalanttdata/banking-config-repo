package com.nttdata.credit_service.service;

import com.nttdata.credit_service.model.Credit;
import com.nttdata.credit_service.repository.CreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service @RequiredArgsConstructor
public class CreditService {

    private final CreditRepository repository;

    public Flux<Credit> findAll() { return repository.findAll(); }
    public Mono<Credit> findById(String id) { return repository.findById(id); }

    public Mono<Credit> create(Credit c) {
        if (c.getBalance() == null) c.setBalance(c.getAmount());
        // Aquí podrías validar: if PERSONAL && count >=1 -> error
        return repository.save(c);
    }
    public Mono<Credit> update(String id, Credit c) {
        return repository.findById(id).flatMap(db -> {
            db.setType(c.getType());
            db.setAmount(c.getAmount());
            db.setBalance(c.getBalance());
            return repository.save(db);
        });
    }
    public Mono<Void> delete(String id) { return repository.deleteById(id); }

    public Mono<Credit> pay(String id, BigDecimal amount) {
        return repository.findById(id)
                .flatMap(cr -> {
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) return Mono.error(new IllegalArgumentException("Invalid amount"));
                    var newBalance = cr.getBalance().subtract(amount);
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) newBalance = BigDecimal.ZERO;
                    cr.setBalance(newBalance);
                    return repository.save(cr);
                });
    }
}