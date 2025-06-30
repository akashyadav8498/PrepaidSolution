package com.example.PrepaidSolution.components.messaging;

import com.example.PrepaidSolution.util.UtilityServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    @Autowired
    UtilityServices utilityServices;

//    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receive(String message) {
        System.out.println("Received message: " + message);
        utilityServices.decodeAndSave(message);
    }
}
