package com.nttdata.card_service.repository;

import com.nttdata.card_service.model.CreditCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CreditCardRepository extends ReactiveMongoRepository<CreditCard, String> { }