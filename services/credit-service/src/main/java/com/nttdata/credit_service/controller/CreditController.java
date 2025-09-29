package com.nttdata.credit_service.controller;

import com.nttdata.credit_service.model.Credit;
import com.nttdata.credit_service.service.CreditService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService service;

    @GetMapping public Flux<Credit> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Mono<Credit> findById(@PathVariable String id) { return service.findById(id); }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping public Mono<Credit> create(@RequestBody Credit c) { return service.create(c); }

    @PutMapping("/{id}") public Mono<Credit> update(@PathVariable String id, @RequestBody Credit c) {
        return service.update(id, c);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}") public Mono<Void> delete(@PathVariable String id) { return service.delete(id); }

    @PostMapping("/{id}/pay")
    public Mono<Credit> pay(@PathVariable String id, @RequestBody PayRequest req) {
        return service.pay(id, req.getAmount());
    }

    @Data
    public static class PayRequest {
        private BigDecimal amount;
    }
}
