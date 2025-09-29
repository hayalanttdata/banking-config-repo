package com.nttdata.transaction_service.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    private String description;
    private BigDecimal amount;

    private TransactionType type;

    private String productId;
    private String productType;

    private LocalDateTime occurredAt;

}