package com.example.PrepaidSolution.service;

// Marks this as a service (Spring will manage this object)
import org.springframework.stereotype.Service;

// Used to send messages via WebSocket
import org.springframework.messaging.simp.SimpMessagingTemplate;

// Inject dependency automatically
import org.springframework.beans.factory.annotation.Autowired;


@Service // This class contains business logic
public class NotificationService {

    // Spring injects this internally (you don't create it manually)
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Sends notification when money is added
    public void sendBalanceAdded(Long tenantId, String name, double amount, double newBalance) {

        String message = "💰 Amount added: ₹" + amount + " | New Balance: ₹" + newBalance + " | Tenant Name: " + name;

        messagingTemplate.convertAndSend(
                "/topic/balance-added/" + tenantId,
                message
        );
    }

    // This method will send notification to frontend
    public void sendLowBalanceAlert(Long tenantId, String name, double balance) {

        // Create message text
        String message = "⚠️ Low balance: " + balance + " | Tenant Name: " + name;

        // This sends message to WebSocket broker
        // Under the hood:
        // 1. Message goes to broker
        // 2. Broker finds subscribers
        // 3. Sends to frontend instantly
        messagingTemplate.convertAndSend(
                "/topic/low-balance/" + tenantId,
                message
        );
    }
}
