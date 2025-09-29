package com.nttdata.customer_service.controller;


import com.nttdata.customer_service.model.Customer;
import com.nttdata.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository repository;

    @GetMapping
    public Flux<Customer> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Customer> findById(@PathVariable String id) {
        return repository.findById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<Customer> create(@Validated @RequestBody Customer customer) {
        return repository.save(customer);
    }

    @PutMapping("/{id}")
    public Mono<Customer> update(@PathVariable String id, @Validated @RequestBody Customer dto) {
        return repository.findById(id)
                .flatMap(db -> {
                    db.setName(dto.getName());
                    db.setType(dto.getType()); // PERSONAL|BUSINESS
                    db.setDocumentNumber(dto.getDocumentNumber());
                    return repository.save(db);
                });
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return repository.deleteById(id);
    }
}