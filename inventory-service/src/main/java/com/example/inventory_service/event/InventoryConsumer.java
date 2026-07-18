package com.example.inventory_service.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.inventory_service.config.RabbitMQConfig;

@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);

    // ubah parameternya supaya otomatis menangkap tipe data OrderCreatedEvent
    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void consumeMessage(OrderCreatedEvent event) {

        log.info("Rawrrrrr!!! Pesan Order masuk dan berhasil ditangkap: {}", event);

    }
}