package com.example.PrepaidSolution.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    public static final String QUEUE = "bridge.main.queue";
    public static final String EXCHANGE = "bridge.main.exchange";
    public static final String ROUTING = "bridge.main.routing.key";

    @Bean
    public Queue meterQueue() {
        return QueueBuilder
                .durable(QUEUE)
                .build();
    }

    @Bean
    public DirectExchange meterExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(meterQueue())
                .to(meterExchange())
                .with(ROUTING);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);

        template.setConfirmCallback((correlation, ack, cause) -> {
            if (!ack) {
                log.error("❌ RabbitMQ NACK: {}", cause);
            }
        });

        template.setReturnsCallback(returned ->
                log.error("❌ Returned message: {}", returned.getMessage())
        );

        return template;
    }
}
