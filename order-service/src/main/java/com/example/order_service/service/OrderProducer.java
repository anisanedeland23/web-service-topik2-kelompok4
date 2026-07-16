package com.example.order_service.service;

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
        log.info("[PRODUCER] Sending OrderCreatedEvent for order {}", event.getOrderId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setReplyTo("inventory.reply");
                    return message;
                });
        log.info("[PRODUCER] Event sent with replyTo='inventory.reply'");
    }
}
