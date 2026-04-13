package com.example.PrepaidSolution.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    // ============================
    // PRIMARY KEY
    // ============================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================
    // WHO WILL RECEIVE
    // ============================

    // OwnerId OR TenantId
    @Column(nullable = false)
    private Long recipientId;

    // OWNER / TENANT
    @Column(nullable = false)
    private String recipientType;


    // ============================
    //  NOTIFICATION DATA
    // ============================

    // For display (useful for owner UI)
    private String tenantName;

    // LOW_BALANCE / BALANCE_ADDED
    private String type;

    // Full message (ready to show)
    @Column(nullable = false)
    private String message;


    // ============================
    //  STATUS
    // ============================

    // For unread badge
    private boolean isRead = false;


    // ============================
    // TIMESTAMP
    // ============================

    private LocalDateTime createdAt = LocalDateTime.now();


    // ============================
    //  GETTERS
    // ============================

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    // ============================
    // SETTERS
    // ============================

    public void setId(Long id) {
        this.id = id;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}