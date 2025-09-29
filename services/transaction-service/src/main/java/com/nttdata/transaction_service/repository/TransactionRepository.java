package com.nttdata.transaction_service.repository;

import com.nttdata.transaction_service.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByProductIdOrderByTimestampDesc(String productId);
}