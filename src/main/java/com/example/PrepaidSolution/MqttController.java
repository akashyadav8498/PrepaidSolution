package com.example.PrepaidSolution;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class MqttController {

    private final MqttService mqttService;

    public MqttController(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @PostMapping("/publish")
    public String publish(@RequestParam String message) {
        try {
            mqttService.publish(message);
            return "Message published";
        } catch (MqttException e) {
            e.printStackTrace();
            return "Failed to publish message";
        }
    }
}

