package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.model.Balance;
import com.example.PrepaidSolution.model.Tenant;
import com.example.PrepaidSolution.repository.BalanceRepository;

import com.example.PrepaidSolution.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

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



    // MAIN METHOD
    public void updateBalance(Long tenantId, double newBalance) {

        // Fetch balance
        Balance balance = balanceRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        // Update balance + time
        balance.setCurrentBalance(newBalance);
        balance.setUpdatedAt(LocalDateTime.now());

        // Fetch tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        String name = tenant.getName();

        // Null safety check for relationship chain
        if (tenant.getRoom() == null ||
                tenant.getRoom().getPg() == null ||
                tenant.getRoom().getPg().getOwner() == null) {

            throw new RuntimeException("Owner mapping not found for tenant");
        }

        // Get ownerId (as per your structure)
        Long ownerId = tenant.getRoom().getPg().getOwner().getId();

        // ============================
        // LOW BALANCE TRIGGER
        // ============================
        if (newBalance < 100 && !balance.isLowBalanceNotified()) {

            notificationService.sendLowBalanceAlert(
                    tenantId,
                    ownerId,
                    name,
                    newBalance
            );

            // Mark as notified
            balance.setLowBalanceNotified(true);
        }

        // If balance becomes healthy again → reset flag
        if (newBalance >= 100 && balance.isLowBalanceNotified()) {

            balance.setLowBalanceNotified(false);
        }

        // Save final state
        balanceRepository.save(balance);
    }


    // ADD MONEY
    public double  addBalance(Long tenantId, double amount) {

        // Get balance
        Balance balance = balanceRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        //  Get tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        //  Extract name
        String name = tenant.getName();

        // Get ownerId
        Long ownerId = tenant.getRoom().getPg().getOwner().getId();

        double current = balance.getCurrentBalance();
        double newBalance = current + amount;

        // Send notification (NEW DTO METHOD)
        notificationService.sendBalanceAdded(
                tenantId,
                ownerId,
                name,
                amount,
                newBalance
        );

        // Update balance
        updateBalance(tenantId, newBalance);
        return newBalance;
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