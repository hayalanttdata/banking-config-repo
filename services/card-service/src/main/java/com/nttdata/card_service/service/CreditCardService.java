package com.nttdata.card_service.service;

import com.nttdata.card_service.model.CreditCard;
import com.nttdata.card_service.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service @RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository repository;

    public Flux<CreditCard> findAll() { return repository.findAll(); }
    public Mono<CreditCard> findById(String id) { return repository.findById(id); }
    public Mono<CreditCard> create(CreditCard c) {
        if (c.getUsed() == null) c.setUsed(BigDecimal.ZERO);
        return repository.save(c);
    }
    public Mono<CreditCard> update(String id, CreditCard c) {
        return repository.findById(id).flatMap(db -> {
            db.setType(c.getType());
            db.setCreditLimit(c.getCreditLimit());
            return repository.save(db);
        });
    }
    public Mono<Void> delete(String id) { return repository.deleteById(id); }

    public Mono<CreditCard> charge(String id, BigDecimal amount) {
        return repository.findById(id).flatMap(card -> {
            var newUsed = card.getUsed().add(amount);
            if (newUsed.compareTo(card.getCreditLimit()) > 0) {
                return Mono.error(new IllegalStateException("Limit exceeded"));
            }
            card.setUsed(newUsed);
            return repository.save(card);
        });
    }

    public Mono<CreditCard> pay(String id, BigDecimal amount) {
        return repository.findById(id).flatMap(card -> {
            var newUsed = card.getUsed().subtract(amount);
            if (newUsed.compareTo(BigDecimal.ZERO) < 0) newUsed = BigDecimal.ZERO;
            card.setUsed(newUsed);
            return repository.save(card);
        });
    }

    public Mono<BigDecimal> available(String id) {
        return repository.findById(id).map(card -> card.getCreditLimit().subtract(card.getUsed()));
    }
}