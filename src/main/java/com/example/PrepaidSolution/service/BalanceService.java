package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.model.Balance;
import com.example.PrepaidSolution.model.Tenant;
import com.example.PrepaidSolution.repository.BalanceRepository;

import com.example.PrepaidSolution.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service // Marks this as business logic layer
public class BalanceService {

    // Used to interact with DB
    @Autowired
    private BalanceRepository balanceRepository;

    // Used to send notification
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TenantRepository tenantRepository;


    // ==========================================
    // MAIN METHOD (MOST IMPORTANT)
    // ==========================================
    public void updateBalance(Long tenantId, double newBalance) {

        // Fetch balance from DB using primary key
        // Under the hood:
        // → Hibernate runs SELECT query
        Balance balance = balanceRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        // Update balance in object (still not saved to DB)
        balance.setCurrentBalance(newBalance);


        // =============================
        // LOW BALANCE LOGIC
        // =============================

        // Condition:
        // 1. Balance < 100
        // 2. Notification not already sent
        System.out.println("Checking low balance for tenant: " + tenantId + " balance: " + newBalance);
        if (newBalance < 100 && !balance.isLowBalanceNotified()) {

            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));

            String name = tenant.getName();

            // Send WebSocket notification
            notificationService.sendLowBalanceAlert(tenantId, name, newBalance);

            // Mark as notified to prevent spam
            balance.setLowBalanceNotified(true);
        }

        // Reset flag if balance becomes normal again
        if (newBalance >= 100) {
            balance.setLowBalanceNotified(false);
        }

        // Save updated object to DB
        // Under the hood:
        // → Hibernate generates UPDATE query
        balanceRepository.save(balance);
    }


    // ==========================================
    // ADD MONEY (FAKE PAYMENT)
    // ==========================================
    public void addBalance(Long tenantId, double amount) {

        // Fetch existing balance
        Balance balance = balanceRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        // Fetch tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        String name = tenant.getName();

        // Get current balance
        double current = balance.getCurrentBalance();

        // Add amount
        double newBalance = current + amount;


        // SEND NOTIFICATION FOR ADD
        notificationService.sendBalanceAdded(tenantId, name, amount, newBalance);

        // Reuse update logic (VERY IMPORTANT DESIGN)
        updateBalance(tenantId, newBalance);
    }


    // ==========================================
    // DEDUCT MONEY
    // ==========================================
    public void deductBalance(Long tenantId, double amount) {

        Balance balance = balanceRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        double current = balance.getCurrentBalance();

        double newBalance = current - amount;

        // Reuse update logic
        updateBalance(tenantId, newBalance);
    }
}