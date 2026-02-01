package com.example.PrepaidSolution.config;

import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Configuration
public class MqttIngestQueueConfig {

    private final BlockingQueue<byte[]> mqttQueue =
            new ArrayBlockingQueue<>(10_000);

}
