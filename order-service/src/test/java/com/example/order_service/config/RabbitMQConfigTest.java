package com.example.order_service.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

class RabbitMQConfigTest {

    @Test
    void shouldCreateRabbitAdminBean() {
        RabbitMQConfig config = new RabbitMQConfig();
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");

        AmqpAdmin admin = config.amqpAdmin(connectionFactory);

        assertThat(admin).isInstanceOf(RabbitAdmin.class);
    }
}
