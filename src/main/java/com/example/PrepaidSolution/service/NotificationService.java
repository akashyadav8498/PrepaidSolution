package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.dto.NotificationPayload;
import com.example.PrepaidSolution.model.Notification;
import com.example.PrepaidSolution.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    // Sends notification when money is added
    public void sendBalanceAdded(Long tenantId, Long ownerId, String name, double amount, double newBalance) {

        String message = name + " has added ₹ " + amount + ". Balance: ₹" + newBalance;

        // create DTO payload
        NotificationPayload payload = new NotificationPayload(
                "BALANCE_ADDED",
                message,
                name
        );

        // save for tenant
        Notification tenantNotification = new Notification();
        tenantNotification.setRecipientId(tenantId);
        tenantNotification.setRecipientType("TENANT");
        tenantNotification.setTenantName(name);
        tenantNotification.setType("BALANCE_ADDED");
        tenantNotification.setMessage(message);
        notificationRepository.save(tenantNotification);


        // save for owner
        Notification ownerNotification = new Notification();
        ownerNotification.setRecipientId(ownerId);
        ownerNotification.setRecipientType("OWNER");
        ownerNotification.setTenantName(name);
        ownerNotification.setType("BALANCE_ADDED");
        ownerNotification.setMessage(message);
        notificationRepository.save(ownerNotification);

        messagingTemplate.convertAndSend("/topic/tenant/" + tenantId, payload);
        messagingTemplate.convertAndSend("/topic/owner/" + ownerId, payload);
    }

    // Sends low balance notification using structured DTO
    public void sendLowBalanceAlert(Long tenantId, Long ownerId, String name, double balance) {

        // Create message string
        String message = name + "has Low balance: ₹" + balance;

        // create payload for websocket
        NotificationPayload payload = new NotificationPayload(
                "LOW_BALANCE",  // type
                message,        // message
                name            // tenant name
        );

        // save for tenant
        Notification tenantNotification = new Notification();
        tenantNotification.setRecipientId(tenantId);
        tenantNotification.setRecipientType("TENANT");
        tenantNotification.setTenantName(name);
        tenantNotification.setType("LOW_BALANCE");
        tenantNotification.setMessage(message);
        notificationRepository.save(tenantNotification);

        // save for owner
        Notification ownerNotification = new Notification();
        ownerNotification.setRecipientId(ownerId);
        ownerNotification.setRecipientType("OWNER");
        ownerNotification.setTenantName(name);
        ownerNotification.setType("LOW_BALANCE");
        ownerNotification.setMessage(message);
        notificationRepository.save(ownerNotification);

        // send realtime
        messagingTemplate.convertAndSend("/topic/tenant/" + tenantId, payload);
        messagingTemplate.convertAndSend("/topic/owner/" + ownerId, payload);
    }
}
