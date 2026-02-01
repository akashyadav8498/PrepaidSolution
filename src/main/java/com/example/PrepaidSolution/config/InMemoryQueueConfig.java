package com.example.PrepaidSolution.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Configuration
public class InMemoryQueueConfig {

    @Bean
    public BlockingQueue<String> dbQueue() {
        // Buffer between RabbitMQ and DB
        return new ArrayBlockingQueue<>(20_000);
    }
}
