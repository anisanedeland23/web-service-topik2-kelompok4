package com.example.order_service.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.order_service.dto.OrderItemResponse;
import com.example.order_service.dto.OrderRequest;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.event.OrderCreatedEvent;
import com.example.order_service.model.Order;

@Component
public class OrderMapper {

    public Order toEntity(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        return order;
    }

    public OrderResponse toResponse(Order order, List<OrderItemResponse> items) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                items,
                order.getTotalAmount(),
                order.getStatus().toString(),
                order.getCreatedAt());
    }

    public OrderCreatedEvent toEvent(Order order, OrderRequest request) {
        List<OrderCreatedEvent.OrderItem> items = request.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderItem(
                        i.getProductId(), i.getProductName(), i.getQuantity(), i.getPrice()))
                .toList();
        return new OrderCreatedEvent(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                items,
                order.getTotalAmount(),
                order.getCreatedAt());
    }
}