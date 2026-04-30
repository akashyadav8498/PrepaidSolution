package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.service.BalanceService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


@org.springframework.web.bind.annotation.RestController // Marks this class as REST API controller
@RequestMapping("/balance") // Base URL for all APIs in this controller
public class BalanceController {

    // Injects BalanceService (business logic layer)
    @Autowired
    private BalanceService balanceService;


    // ==========================================
    // ADD MONEY (FAKE PAYMENT)
    // ==========================================
    @PostMapping("/add")
    public Map<String,Object> addBalance(@RequestBody Map<String, Object> request) {

        // Extract tenantId from JSON
        Long tenantId = Long.valueOf(request.get("tenantId").toString());

        // Extract amount from JSON
        double amount = Double.parseDouble(request.get("amount").toString());

        // Call service to add balance
        double updatedBalance = balanceService.addBalance(tenantId, amount);

        return Map.of(
                "message", "Balance added successfully",
                "balance", updatedBalance
        );
    }


    // ==========================================
    // DEDUCT MONEY
    // ==========================================
    @PostMapping("/deduct")
    public String deductBalance(@RequestBody Map<String, Object> request) {

        // Extract tenantId
        Long tenantId = Long.valueOf(request.get("tenantId").toString());

        // Extract amount
        double amount = Double.parseDouble(request.get("amount").toString());

        // Call service to deduct balance
        balanceService.deductBalance(tenantId, amount);

        return "Balance deducted successfully";
    }
}