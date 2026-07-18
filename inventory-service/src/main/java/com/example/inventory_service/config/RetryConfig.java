package com.example.inventory_service.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * [ANGGOTA 5] Konfigurasi jalur akhir retry -> DLQ.
 *
 * Alur: consumer melempar exception -> Spring retry beberapa kali (lihat
 * application.properties) -> kalau tetap gagal, recoverer di bawah ini
 * dipanggil. RejectAndDontRequeueRecoverer menolak pesan tanpa requeue,
 * sehingga RabbitMQ memindahkannya ke DLQ lewat argumen
 * "x-dead-letter-exchange" pada inventory-queue.
 */
@Configuration
@Slf4j
public class RetryConfig {

    @Bean
    public MessageRecoverer messageRecoverer() {
        return new RejectAndDontRequeueRecoverer() {
            @Override
            public void recover(Message message, Throwable cause) {
                log.error("[DLQ] Retry sudah habis. Pesan dilempar ke DLQ. Penyebab: {}",
                        cause.getMessage());
                // super akan menolak pesan tanpa requeue -> memicu dead-letter ke DLQ
                super.recover(message, cause);
            }
        };
    }
}
