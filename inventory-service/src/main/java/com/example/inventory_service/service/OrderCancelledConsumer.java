package com.example.inventory_service.service;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.example.inventory_service.event.OrderCancelledEvent;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCancelledConsumer {

    private final InventoryRepository inventoryRepository;

    @RabbitListener(queues = "order.cancelled")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("[CANCEL CONSUMER] Received OrderCancelledEvent for order {}", event.getOrderId());

        for (OrderCancelledEvent.OrderItem item : event.getItems()) {
            Optional<Inventory> opt = inventoryRepository.findByProductId(item.getProductId());
            if (opt.isPresent()) {
                Inventory inv = opt.get();
                int reserved = inv.getReservedQuantity();
                int newReserved = Math.max(0, reserved - item.getQuantity());
                inv.setReservedQuantity(newReserved);
                inventoryRepository.save(inv);
                log.info("[CANCEL CONSUMER] Rolled back {} units for product {}", item.getQuantity(),
                        item.getProductId());
            } else {
                log.warn("[CANCEL CONSUMER] Product {} not found, skipping", item.getProductId());
            }
        }
    }
}