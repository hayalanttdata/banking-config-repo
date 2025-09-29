package com.nttdata.customer_service.service;

import com.nttdata.customer_service.model.Customer;
import com.nttdata.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public Flux<Customer> findAll() {
        return repository.findAll();
    }

    public Mono<Customer> findById(String id) {
        return repository.findById(id);
    }

    public Mono<Customer> save(Customer customer) {
        return repository.save(customer);
    }

    public Mono<Customer> update(String id, Customer customer) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setName(customer.getName());
                    existing.setType(customer.getType());
                    existing.setDocumentNumber(customer.getDocumentNumber());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }
}