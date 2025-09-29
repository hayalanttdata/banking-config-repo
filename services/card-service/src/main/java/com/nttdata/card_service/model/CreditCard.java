package com.nttdata.card_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document(collection = "cards")
public class CreditCard {
    @Id
    private String id;
    private String customerId;
    private String type;             // PERSONAL | BUSINESS
    private BigDecimal creditLimit;  // límite total
    private BigDecimal used;         // monto utilizado
}