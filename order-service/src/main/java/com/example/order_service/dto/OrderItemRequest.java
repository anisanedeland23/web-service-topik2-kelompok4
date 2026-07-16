package com.example.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotBlank
    private String productId;
    private String productName;
    @Positive
    private Integer quantity;
    @Positive
    private Double price;
}
