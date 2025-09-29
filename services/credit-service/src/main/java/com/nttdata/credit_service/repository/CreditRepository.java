package com.nttdata.credit_service.repository;

import com.nttdata.credit_service.model.Credit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {
    Mono<Long> countByCustomerIdAndType(String customerId, String type);
}