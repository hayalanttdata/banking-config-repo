package com.nttdata.transaction_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String productId;     // id de cuenta o tarjeta
    private String productType;   // ACCOUNT | CARD | CREDIT (según lo uses)
    private String operation;     // DEPOSIT | WITHDRAW | CHARGE | PAY
    private BigDecimal amount;
    private Instant timestamp;
    private String description;
}