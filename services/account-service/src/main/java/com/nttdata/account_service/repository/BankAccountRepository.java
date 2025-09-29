package com.nttdata.account_service.repository;

import com.nttdata.account_service.model.BankAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BankAccountRepository extends ReactiveMongoRepository<BankAccount, String> { }