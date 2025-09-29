package com.nttdata.credit_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document(collection = "credits")
public class Credit {
    @Id
    private String id;
    private String customerId;
    private String type;          // PERSONAL | BUSINESS
    private BigDecimal amount;    // monto del crédito
    private BigDecimal balance;   // saldo por pagar
}