package com.example.PrepaidSolution.controller;


import ch.qos.logback.core.model.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MVCController
{
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        return "/admin.html";
    }
    @GetMapping("/owner/dashboard")
    @PreAuthorize("hasRole('OWNER')")
    public String ownerDashboard(Model model) {
        return "/owner.html";
    }
    @GetMapping("/tenant/dashboard")
    @PreAuthorize("hasRole('TENANT')")
    public String tenantDashboard(Model model){
        return "/tenant.html";
    }
}