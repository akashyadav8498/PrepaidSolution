package com.example.PrepaidSolution.messaging;

import com.example.PrepaidSolution.config.RabbitMQConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttToRabbitWorker {

    private final BlockingQueue<String> mqttQueue;
    private final RabbitTemplate rabbitTemplate;

    private final AtomicLong received = new AtomicLong();
    private final AtomicLong sent = new AtomicLong();

    @PostConstruct
    public void start() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                try {
                    String msg = mqttQueue.take();
                    received.incrementAndGet();

                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.EXCHANGE,
                            RabbitMQConfig.ROUTING,
                            msg
                    );

                    sent.incrementAndGet();
                } catch (Exception e) {
                    log.error("❌ Worker error", e);
                }
            }
        });
    }

    @Scheduled(fixedRate = 10000)
    public void stats() {
        log.info("📊 MQTT→Rabbit | received={} sent={} queue={}",
                received.get(), sent.get(), mqttQueue.size());
    }
}

