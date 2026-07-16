package com.example.inventory_service.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent {
    private Long orderId;
    private String productId;
    private Integer quantity;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;
}