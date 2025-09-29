package com.nttdata.card_service.controller;

import com.nttdata.card_service.model.CreditCard;
import com.nttdata.card_service.service.CreditCardService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService service;

    @GetMapping public Flux<CreditCard> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Mono<CreditCard> findById(@PathVariable String id) { return service.findById(id); }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping public Mono<CreditCard> create(@RequestBody CreditCard c) { return service.create(c); }

    @PutMapping("/{id}") public Mono<CreditCard> update(@PathVariable String id, @RequestBody CreditCard c) {
        return service.update(id, c);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}") public Mono<Void> delete(@PathVariable String id) { return service.delete(id); }

    @PostMapping("/{id}/charge")
    public Mono<CreditCard> charge(@PathVariable String id, @RequestBody AmountRequest req) {
        return service.charge(id, req.getAmount());
    }

    @PostMapping("/{id}/pay")
    public Mono<CreditCard> pay(@PathVariable String id, @RequestBody AmountRequest req) {
        return service.pay(id, req.getAmount());
    }

    @GetMapping("/{id}/balance")
    public Mono<Map<String, BigDecimal>> balance(@PathVariable String id) {
        return service.available(id).map(av -> Map.of("available", av));
    }

    @Data
    public static class AmountRequest {
        private BigDecimal amount;
    }
}