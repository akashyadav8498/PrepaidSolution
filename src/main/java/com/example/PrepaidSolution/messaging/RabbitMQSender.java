package com.example.PrepaidSolution.messaging;

import com.example.PrepaidSolution.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(Object message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING,
                message
        );
    }
}
