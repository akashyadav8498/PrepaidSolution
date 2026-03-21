package com.example.PrepaidSolution.messaging;

import com.example.PrepaidSolution.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMQListener {


    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receive(String message) {
        log.info("🐇 RabbitMQ Received: {}", message);
    }
}