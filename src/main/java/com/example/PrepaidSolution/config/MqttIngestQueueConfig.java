package com.example.PrepaidSolution.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class MqttIngestQueueConfig {

    @Bean
    public BlockingQueue<String> mqttQueue() {
        // Size depends on burst rate; safe default
        return new LinkedBlockingQueue<>(100_000);
    }
}
