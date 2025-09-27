package com.example.PrepaidSolution.controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MvcErrorController {

    @GetMapping("/forbidden")
    public String ownerDashboard(Model model) {
        return "/forbidden.html";
    }
}
