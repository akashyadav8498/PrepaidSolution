package com.example.PrepaidSolution.messaging;

import com.example.PrepaidSolution.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQListener {

    private final BlockingQueue<String> dbQueue;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receive(String message) {
        log.info("🐇 RabbitMQ Received: {}", message);

        boolean accepted = dbQueue.offer(message);
        if (!accepted) {
            log.error("❌ DB QUEUE FULL — DROPPING MESSAGE");
        }
    }
}