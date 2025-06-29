package com.example.PrepaidSolution.messaging;

import com.example.PrepaidSolution.config.RabbitMQConfig;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class MQTTSubscriber {

    @Value("${mqtt.broker.serverHost}")
    private String mqttServerHost;

    @Value("${mqtt.broker.serverPort}")
    private Integer mqttServerPort;

    @Value("${mqtt.topic}")
    private String topic;

    private Mqtt3AsyncClient client;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @PostConstruct
    public void init() {
        client = MqttClient.builder()
                .identifier("spring-" + UUID.randomUUID())
                .serverHost(mqttServerHost)
                .serverPort(mqttServerPort)
                .useMqttVersion3()
                .buildAsync();

        client.connectWith()
                .cleanSession(true)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable == null) {
                        System.out.println("Connected to MQTT broker");

                        client.subscribeWith()
                                .topicFilter(topic)
                                .callback(publish -> {
                                    String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                                    System.out.println("Received message: " + payload);
                                    sendMessage(payload);
                                })
                                .send();
                    } else {
                        System.err.println("Connection failed: " + throwable.getMessage());
                    }
                });
    }

    public void sendMessage(String message) {
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
    }
}
