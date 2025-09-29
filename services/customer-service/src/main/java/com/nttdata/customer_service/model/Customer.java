package com.nttdata.customer_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
    @Id
    private String id;
    private String name;
    private String type; // PERSONAL or BUSINESS
    private String documentNumber;
}
