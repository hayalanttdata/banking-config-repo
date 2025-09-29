package com.nttdata.transaction_service.controller;

import com.nttdata.transaction_service.model.Transaction;
import com.nttdata.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService service;

    @GetMapping
    public Flux<Transaction> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public Mono<Transaction> findById(@PathVariable String id) { return service.findById(id); }

    @GetMapping("/by-product/{productId}")
    public Flux<Transaction> findByProduct(@PathVariable String productId) {
        return service.findByProduct(productId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<Transaction> create(@Valid @RequestBody Transaction t) { return service.create(t); }

    @PutMapping("/{id}")
    public Mono<Transaction> update(@PathVariable String id, @Valid @RequestBody Transaction t) {
        return service.update(id, t);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return service.delete(id);
    }
}