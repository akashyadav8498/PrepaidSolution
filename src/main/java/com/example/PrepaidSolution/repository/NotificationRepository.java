package com.example.PrepaidSolution.repository;

import com.example.PrepaidSolution.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a user (latest first)
    List<Notification> findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
            Long recipientId,
            String recipientType
    );

    // Get unread notifications
    List<Notification> findByRecipientIdAndRecipientTypeAndIsReadFalse(
            Long recipientId,
            String recipientType
    );

    List<Notification> findTop20ByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
            Long recipientId,
            String recipientType
    );

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.recipientType = :recipientType AND n.isRead = false")
    void markAllAsRead(Long recipientId, String recipientType);
}