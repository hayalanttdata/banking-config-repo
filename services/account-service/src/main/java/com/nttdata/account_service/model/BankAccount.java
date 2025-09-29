package com.nttdata.account_service.model;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document(collection = "accounts")
public class BankAccount {
    @Id
    private String id;
    private String customerId;   // referencia lógica (sin FK entre servicios)
    private String type;         // SAVINGS | CURRENT | FIXED_TERM
    private BigDecimal balance;  // saldo
    private BigDecimal maintenanceFee; // para CURRENT
    private Integer monthlyMovementLimit; // para SAVINGS
}