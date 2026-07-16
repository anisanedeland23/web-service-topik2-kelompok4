package com.example.order_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.order_service.config.RabbitMQConfig;
import com.example.order_service.event.InventoryUpdatedEvent;
import com.example.order_service.event.OrderCancelledEvent;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConsumerReply {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "inventory.reply")
    public void handleInventoryReply(InventoryUpdatedEvent event) {
        log.info("[ORDER REPLY] Received InventoryUpdatedEvent for order {}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (event.getSuccess()) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            log.info("[ORDER REPLY] Order {} confirmed", event.getOrderId());
        } else {
            order.setStatus(Order.OrderStatus.FAILED);
            log.info("[ORDER REPLY] Order {} failed", event.getOrderId());

            try {
                List<OrderCancelledEvent.OrderItem> items = objectMapper.readValue(
                        order.getItems(),
                        new TypeReference<List<OrderCancelledEvent.OrderItem>>() {
                        });

                OrderCancelledEvent cancelEvent = new OrderCancelledEvent(
                        event.getOrderId(),
                        items,
                        "Inventory processing failed",
                        LocalDateTime.now());
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_EXCHANGE,
                        RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY,
                        cancelEvent);
                log.info("[ORDER REPLY] Sent OrderCancelledEvent for order {}", event.getOrderId());
            } catch (Exception e) {
                log.error("[ORDER REPLY] Failed to send OrderCancelledEvent: {}", e.getMessage(), e);
            }
        }

        orderRepository.save(order);
    }
}