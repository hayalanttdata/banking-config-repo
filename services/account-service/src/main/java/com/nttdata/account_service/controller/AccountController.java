package com.nttdata.account_service.controller;

import com.nttdata.account_service.model.BankAccount;
import com.nttdata.account_service.service.AccountService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @GetMapping public Flux<BankAccount> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Mono<BankAccount> findById(@PathVariable String id) { return service.findById(id); }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping public Mono<BankAccount> create(@RequestBody BankAccount a) { return service.create(a); }

    @PutMapping("/{id}") public Mono<BankAccount> update(@PathVariable String id, @RequestBody BankAccount a) {
        return service.update(id, a);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}") public Mono<Void> delete(@PathVariable String id) { return service.delete(id); }

    @PostMapping("/{id}/deposit")
    public Mono<BankAccount> deposit(@PathVariable String id, @RequestBody AmountRequest req) {
        return service.deposit(id, req.getAmount());
    }

    @PostMapping("/{id}/withdraw")
    public Mono<BankAccount> withdraw(@PathVariable String id, @RequestBody AmountRequest req) {
        return service.withdraw(id, req.getAmount());
    }

    @Data
    public static class AmountRequest {
        private BigDecimal amount;
    }
}