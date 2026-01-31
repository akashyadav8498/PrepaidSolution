package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.config.AWSIOTMQTTConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
public class IndexController {

    @Autowired
    private AWSIOTMQTTConfig awsiotmqttConfig;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> payload, HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
//        session.setAttribute("userName", );
        return ResponseEntity.ok(Map.of("uri","/meter_management"));
    }

//    @GetMapping("/get_live_water_level")
//    public ResponseEntity<?> getLiveWaterLevel() {
//
//            try {
//                MqttClientConnection connection = awsiotmqttConfig.getConnection();
//                connection.subscribe(
//                        "esp32_pub",
//                        QualityOfService.AT_LEAST_ONCE,
//                        (MqttMessage message) -> {
//                            byte[] payload = message.getPayload();
//                            System.out.println("Topic: " + message.getTopic());
//                            System.out.println("Received: " + new String(payload, StandardCharsets.UTF_8));
//                        }
//                );
//
//
//
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//        return ResponseEntity.ok(Map.of("data",new String(payload, StandardCharsets.UTF_8)));
//    }
}
