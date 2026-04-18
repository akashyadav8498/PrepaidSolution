package com.example.PrepaidSolution.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.PrepaidSolution.service.TenantService;

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
}
