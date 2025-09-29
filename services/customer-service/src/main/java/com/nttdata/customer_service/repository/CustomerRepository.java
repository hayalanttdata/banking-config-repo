package com.nttdata.customer_service.repository;

import com.nttdata.customer_service.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> { }
