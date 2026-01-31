package com.example.PrepaidSolution.config;

import com.example.PrepaidSolution.messaging.RabbitMQSender;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

@Component
@Getter
@RequiredArgsConstructor
@Slf4j
public class AWSIOTMQTTConfig {

    private MqttClientConnection connection;

    private final RabbitMQSender rabbitMQSender;

    private final BlockingQueue<String> mqttQueue;

    @Value("${aws.iot.certPath}")
    private String certPath;

    @Value("${aws.iot.keyPath}")
    private String keyPath;

    @PostConstruct
    public void init() throws Exception {

        String endpoint = "aq3hbf780ob41-ats.iot.ap-south-1.amazonaws.com";
        String clientId = "prepaidsolution-software-client";

        try (AwsIotMqttConnectionBuilder builder =
                     AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(
                             certPath,
                             keyPath
                     )) {

            builder.withEndpoint(endpoint)
                    .withClientId(clientId)
                    .withCleanSession(true);

            connection = builder.build();
        }

        connection.connect().get();
        System.out.println("✅ AWS IoT Connected");
        subscribe();

    }

    @PreDestroy
    public void shutdown() throws Exception {
        if (connection != null) {
            connection.disconnect().get();
            System.out.println("🔌 AWS IoT Disconnected");
        }
    }


    public void subscribe() {
        try {

            connection.subscribe(
                    "esp32_pub",
                    QualityOfService.AT_LEAST_ONCE,
                    (MqttMessage message) -> {

                        byte[] payload = message.getPayload();

                        StringBuilder hex = new StringBuilder();
                        for (byte b : payload) {
                            hex.append(String.format("%02X", b));
                        }

                        String hexString = hex.toString();
                        log.info("Packet --> " + hexString);
                        mqttQueue.offer(hexString); // 🚀 FAST, non-blocking
                    }
            );




        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String topic, String messageStr) {
        try {
            MqttMessage message = new MqttMessage(topic, messageStr.getBytes(StandardCharsets.UTF_8), QualityOfService.AT_LEAST_ONCE);
            connection.publish(message);
            System.out.println("✅ Published message: " + messageStr + " to topic: " + topic);
        } catch (Exception e) {
            System.err.println("❌ Publish failed: " + e.getMessage());
        }
    }
}