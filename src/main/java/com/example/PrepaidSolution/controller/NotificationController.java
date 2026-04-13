package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.Notification;
import com.example.PrepaidSolution.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/latest")
    public List<Notification> getLatestNotifications(
            @RequestParam Long recipientId,
            @RequestParam String recipientType
    ){

        return notificationRepository
                .findTop20ByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
                        recipientId,
                        recipientType
                );
    }

    @PostMapping("/mark-read")
    public String markAsRead(@RequestParam Long recipientId, @RequestParam String recipientType) {
        notificationRepository.markAllAsRead(recipientId, recipientType);
        return "Marked as read";
    }

    // fetch all notifications for user
    @GetMapping
    public List<Notification> getNotifications(
            @RequestParam Long recipientId,
            @RequestParam String recipientType
    ) {

        return notificationRepository
                .findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
                        recipientId,
                        recipientType
                );
    }


    // fetch unread notifications count
    @GetMapping("/unread-count")
    public int getUnreadCount(
            @RequestParam Long recipientId,
            @RequestParam String recipientType
    ) {

        return notificationRepository
                .findByRecipientIdAndRecipientTypeAndIsReadFalse(
                        recipientId,
                        recipientType
                ).size();
    }
}
