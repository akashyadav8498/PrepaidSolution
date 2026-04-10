package com.example.PrepaidSolution.dto;

// This class represents structured notification data instead of plain string
public class NotificationPayload {

    // Type of notification (LOW_BALANCE, BALANCE_ADDED)
    private String type;

    // Actual message to display
    private String message;

    // Tenant name (useful for owner view)
    private String tenantName;

    // Constructor
    public NotificationPayload(String type, String message, String tenantName) {
        this.type = type;
        this.message = message;
        this.tenantName = tenantName;
    }

    // Getters (needed for JSON conversion)
    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getTenantName() {
        return tenantName;
    }
}