package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.service.MeterManagementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.PrepaidSolution.service.TenantService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session){
        session.invalidate();
        return ResponseEntity.ok("Loggout out successfully");
    }

    @GetMapping("/tenant-data")
    public ResponseEntity<?> getTenantData(HttpSession httpSession){
        String email = (String) httpSession.getAttribute("email");
        return ResponseEntity.ok(tenantService.getTenantData(email));
    }

    @GetMapping("/reading/{meterSerialNo}")
    public ResponseEntity<?> getLatestMeterReading(@PathVariable String meterSerialNo) throws JsonProcessingException {
        return ResponseEntity.ok(tenantService.getLatestMeterReading(meterSerialNo));
    }
}
