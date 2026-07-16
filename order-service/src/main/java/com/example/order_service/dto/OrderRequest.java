package com.example.order_service.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String customerName;
    @NotBlank
    private String customerEmail;
    @NotNull
    private List<OrderItemRequest> items;
}