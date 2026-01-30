package com.example.PrepaidSolution.config;

import com.example.PrepaidSolution.messaging.RabbitMQSender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.nio.charset.StandardCharsets;

@Component
@Getter
@RequiredArgsConstructor
public class AWSIOTMQTTConfig {

    public MqttClientConnection connection;

    final RabbitMQSender rabbitMQSender;

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
//        while (true) {
//            sendMessage("esp32_sub", """
//                    {
//                      "data": "from spring boot",
//                      "units":25,
//                      "app":1
//                    }""");
//        }
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
                        System.out.println("Topic: " + message.getTopic());
                        StringBuilder hex = new StringBuilder();
                        for (byte b : payload) {
                            hex.append(String.format("%02X", b));
                        }

                        String hexString = hex.toString();
                        rabbitMQSender.sendMessage(hexString);
                        System.out.println("Received (hex): " + hex);
                    }
            );




        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String topic, String messageStr) {
        try {
            MqttMessage message = new MqttMessage(topic, messageStr.getBytes(StandardCharsets.UTF_8), QualityOfService.AT_LEAST_ONCE);
            connection.publish(message).get();
            System.out.println("✅ Published message: " + messageStr + " to topic: " + topic);
        } catch (Exception e) {
            System.err.println("❌ Publish failed: " + e.getMessage());
        }
    }
}