package com.nttdata.transaction_service.service;

import com.nttdata.transaction_service.model.Transaction;
import com.nttdata.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service @RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;

    public Flux<Transaction> findAll() { return repository.findAll(); }
    public Mono<Transaction> findById(String id) { return repository.findById(id); }
    public Mono<Transaction> create(Transaction t) { return repository.save(t); }
    public Mono<Transaction> update(String id, Transaction t) {
        return repository.findById(id).flatMap(db -> {
            db.setDescription(t.getDescription());
            db.setAmount(t.getAmount());
            db.setOperation(t.getOperation());
            db.setProductId(t.getProductId());
            db.setProductType(t.getProductType());
            db.setTimestamp(t.getTimestamp());
            return repository.save(db);
        });
    }
    public Mono<Void> delete(String id) { return repository.deleteById(id); }

    public Flux<Transaction> findByProduct(String productId) {
        return repository.findByProductIdOrderByOccurredAtDesc(productId);
    }
}