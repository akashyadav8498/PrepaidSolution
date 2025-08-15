package com.example.PrepaidSolution.controller;


import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Controller
@RestController
public class DashboardController
{
//    @GetMapping("/loginError")
//    public String loginError(){
//        return "loginError";
//    }
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        // Add any model attributes here if needed
        return "admin-dashboard"; // Name of the HTML template (e.g., admin-dashboard.html)
    }
    @GetMapping("/owner/dashboard")
    public String ownerDashboard(Model model) {
        return "owner-dashboard";
    }
}