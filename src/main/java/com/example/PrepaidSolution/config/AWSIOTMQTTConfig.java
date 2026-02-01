package com.example.PrepaidSolution.config;

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

@Component
@Getter
@RequiredArgsConstructor
@Slf4j
public class AWSIOTMQTTConfig {

    private MqttClientConnection connection;

    @Value("${aws.iot.certPath}")
    private String certPath;

    @Value("${aws.iot.keyPath}")
    private String keyPath;

    private static final String ENDPOINT =
            "aq3hbf780ob41-ats.iot.ap-south-1.amazonaws.com";

    private static final String CLIENT_ID =
            "prepaidsolution-software-client";

    private static final String TOPIC = "esp32_pub";

    @PostConstruct
    public void init() {
        connectAndSubscribe();
    }

    private synchronized void connectAndSubscribe() {
        try {
            log.info("🔌 Connecting to AWS IoT...");

            AwsIotMqttConnectionBuilder builder =
                    AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(
                            certPath, keyPath
                    );

            builder.withEndpoint(ENDPOINT)
                    .withClientId(CLIENT_ID)
                    .withCleanSession(false); // ❗ important

            connection = builder.build();

            connection.connect().get();
            log.info("✅ AWS IoT Connected");

            subscribe();

        } catch (Exception e) {
            log.error("❌ AWS IoT connection failed", e);
            retryReconnect();
        }
    }

    private void subscribe() {
        try {
            connection.subscribe(
                    TOPIC,
                    QualityOfService.AT_LEAST_ONCE,
                    this::handleMessage
            );

            log.info("📡 Subscribed to topic: {}", TOPIC);

        } catch (Exception e) {
            log.error("❌ Subscription failed", e);
        }
    }

    /**
     * ⚠️ MQTT CALLBACK
     * MUST be FAST & NON-BLOCKING
     */
    private void handleMessage(MqttMessage message) {
        try {
            byte[] payload = message.getPayload();

            log.info("📡 Packet : {}", toHex(payload));

            // 🚀 enqueue only (NO processing here)
//            boolean accepted = mqttWorker.enqueue(payload);

//            if (!accepted) {
//                log.error("❌ MQTT queue full – packet dropped");
//            }

        } catch (Exception e) {
            log.error("❌ Error in MQTT callback", e);
        }
    }

    private void retryReconnect() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    log.warn("🔁 Retrying AWS IoT connection...");
                    connectAndSubscribe();
                    break;
                } catch (Exception ignored) {}
            }
        }).start();
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (connection != null) {
                connection.disconnect().get();
                log.info("🔌 AWS IoT Disconnected");
            }
        } catch (Exception e) {
            log.error("❌ Error during MQTT shutdown", e);
        }
    }

    private String toHex(byte[] payload) {
        StringBuilder sb = new StringBuilder(payload.length * 2);
        for (byte b : payload) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
