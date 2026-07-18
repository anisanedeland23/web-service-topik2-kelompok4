package com.example.inventory_service.event;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);

    public void consumeMessage(OrderCreatedEvent event) {
        log.info("Rawrrrrr!!! Pesan Order masuk dan berhasil ditangkap: {}", event);

    }
}
