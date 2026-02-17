package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.MeterType;
import com.example.PrepaidSolution.model.Owner;
import com.example.PrepaidSolution.model.PG;
import com.example.PrepaidSolution.repository.MeterTypeRepository;
import com.example.PrepaidSolution.repository.OwnerRepository;
import com.example.PrepaidSolution.repository.PGRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mvc/")
public class MVCController {

    private final OwnerRepository ownerRepository;

    private final PGRepository pgRepository;

    private final MeterTypeRepository meterTypeRepository;

    @GetMapping("/login")
    public String login(HttpServletRequest httpServletRequest, Model model) {

        HttpSession session = httpServletRequest.getSession(false);
        String role = (String) session.getAttribute("role");
        String platform = httpServletRequest.getHeader("user-agent");

        if (!role.equalsIgnoreCase("tenant")) {

            List<Owner> owners = ownerRepository.findAll();
            List<PG> pgs = pgRepository.findAll();
            List<MeterType> meterTypes = meterTypeRepository.findAll();

            model.addAttribute("owners", owners);
            model.addAttribute("pgs", pgs);
            model.addAttribute("metertypes", meterTypes);

            if (platform.contains("windows")) return "meter_management";
            else return "meter_management_mobile";
        }
        else
            return "tenant";
    }

}
