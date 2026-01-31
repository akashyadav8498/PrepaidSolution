package com.example.PrepaidSolution.messaging;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class RabbitMQWorker {

    private final BlockingQueue<String> mqttQueue;
    private final RabbitMQSender rabbitMQSender;

    @PostConstruct
    public void startWorkers() {

        int workers = 4; // tune based on CPU & RabbitMQ speed
        ExecutorService executor = Executors.newFixedThreadPool(workers);

        for (int i = 0; i < workers; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        String msg = mqttQueue.take(); // blocks safely
                        rabbitMQSender.sendMessage(msg);
                    } catch (Exception e) {
                        // log + retry logic later
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
