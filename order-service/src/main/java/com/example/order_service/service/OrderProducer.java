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
                    // Kustomisasi Anggota 3 (Producer): 
                    // 1. Memberi nomor resi (ID) unik pada pesan untuk mencegah duplikasi (best practice: idempotency)
                    message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                    // 2. Menempelkan 'stempel pengirim' tambahan (Header) tanpa mengganggu isi pesan utama
                    message.getMessageProperties().setHeader("X-Sender", "Producer");
                    // 3. Memberitahu Consumer agar mengirim surat balasan (hasil stok) ke antrean 'inventory.reply'
                    message.getMessageProperties().setReplyTo("inventory.reply");
                    return message;
                });

        log.info("[PRODUCER] Pesan sukses diteruskan ke antrean dengan replyTo='inventory.reply'");
    }
}
