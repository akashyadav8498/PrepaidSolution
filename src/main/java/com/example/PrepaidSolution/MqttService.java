package com.example.PrepaidSolution;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    private MqttClient mqttClient;

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("outTopic/#")
    private String outTopic;

    @Value("inTopic")
    private String inTopic;

    @PostConstruct
    public void init() throws MqttException {
        mqttClient = new MqttClient(broker, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true); // or false if using persistent session

        // Connect before anything else
        mqttClient.connect(options);

        // Now safe to subscribe
        mqttClient.subscribe(outTopic, (receivedTopic, message) -> {
            System.out.println("Received message: " + new String(message.getPayload()));
        });
    }

    public void publish(String payload) throws MqttException {
        // Always check if connected
        if (!mqttClient.isConnected()) {
            mqttClient.reconnect(); // or throw error / connect again
        }

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1); // optional
        mqttClient.publish(inTopic, message);
    }
}


