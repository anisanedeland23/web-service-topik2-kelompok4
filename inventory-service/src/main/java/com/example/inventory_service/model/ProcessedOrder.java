package com.example.inventory_service.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "processed_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrder {
    @Id
    private String id;
    private Long orderId;
    private LocalDateTime processedAt = LocalDateTime.now();
}