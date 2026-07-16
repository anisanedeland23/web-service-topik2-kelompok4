package com.example.inventory_service.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.inventory_service.config.RabbitMQConfig;
import com.example.inventory_service.event.InventoryUpdatedEvent;
import com.example.inventory_service.event.OrderCreatedEvent;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.model.ProcessedOrder;
import com.example.inventory_service.repository.InventoryRepository;
import com.example.inventory_service.repository.ProcessedOrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {

    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderRepository processedOrderRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "inventory-queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[CONSUMER] Received OrderCreatedEvent for order {}", event.getOrderId());

        if (processedOrderRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("[CONSUMER] Order {} already processed, ignoring duplicate", event.getOrderId());
            return;
        }

        boolean allSuccess = true;

        for (OrderCreatedEvent.OrderItem item : event.getItems()) {
            log.info("[CONSUMER] Processing product {} (qty {})", item.getProductId(), item.getQuantity());

            Optional<Inventory> opt = inventoryRepository.findByProductId(item.getProductId());
            if (opt.isEmpty()) {
                log.warn("[CONSUMER] Product not found: {}", item.getProductId());
                allSuccess = false;
                continue;
            }

            Inventory inv = opt.get();
            int available = inv.getQuantity() - inv.getReservedQuantity();
            if (available < item.getQuantity()) {
                log.warn("[CONSUMER] Insufficient stock for {}: available={}, requested={}",
                        item.getProductId(), available, item.getQuantity());
                allSuccess = false;
                continue;
            }

            inv.setReservedQuantity(inv.getReservedQuantity() + item.getQuantity());
            inventoryRepository.save(inv);
            log.info("[CONSUMER] Reserved {} units for product {}", item.getQuantity(), item.getProductId());
        }

        processedOrderRepository.save(new ProcessedOrder(null, event.getOrderId(), LocalDateTime.now()));

        InventoryUpdatedEvent replyEvent = new InventoryUpdatedEvent(
                event.getOrderId(),
                null,
                null,
                allSuccess,
                allSuccess ? "Reserved successfully" : "Some products failed",
                LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.INVENTORY_UPDATED_ROUTING_KEY,
                replyEvent);
        log.info("[CONSUMER] Sent reply for order {} (success={})", event.getOrderId(), allSuccess);
    }
}