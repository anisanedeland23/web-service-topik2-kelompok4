package com.example.order_service.service;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.order_service.config.RabbitMQConfig;
import com.example.order_service.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("[PRODUCER] Memulai pengiriman data pesanan (ID: {}) ke RabbitMQ...", event.getOrderId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                    message.getMessageProperties().setHeader("X-Sender", "Producer");
                    message.getMessageProperties().setReplyTo("inventory.reply");
                    return message;
                });

        log.info("[PRODUCER] Pesan sukses diteruskan ke antrean dengan replyTo='inventory.reply'");
    }
}
