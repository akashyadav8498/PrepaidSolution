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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MVCController {

    private final OwnerRepository ownerRepository;

    private final PGRepository pgRepository;

    private final MeterTypeRepository meterTypeRepository;

    @GetMapping("/")
    public String handleRootRedirect() {
        // 1. Get the current authentication from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. Check if the user is logged in (and not an anonymous guest)
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            // 3. Logic to check roles and redirect accordingly
            // We check the authorities string (e.g., ROLE_OWNER)
            boolean isOwner = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

            boolean isTenant = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TENANT"));

            if (isOwner) {
                return "redirect:/owner"; // Sends browser to localhost:8080/owner
            } else if (isTenant) {
                return "redirect:/tenant"; // Sends browser to localhost:8080/tenant
            }
        }

        // 4. If NOT logged in, show the landing/login page
        // 'forward' keeps the URL as '/' but shows index.html content
        return "forward:/index.html";
    }

    @GetMapping("/admin")
    public String admin(HttpServletRequest httpServletRequest, Model model) {

        HttpSession session = httpServletRequest.getSession(false);
        String userName = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String platform = httpServletRequest.getHeader("user-agent");

        List<Owner> owners = ownerRepository.findAll();
        List<PG> pgs = pgRepository.findAll();
        List<MeterType> meterTypes = meterTypeRepository.findAll();

        model.addAttribute("owners", owners);
        model.addAttribute("pgs", pgs);
        model.addAttribute("metertypes", meterTypes);
        model.addAttribute("userName", userName);
        model.addAttribute("role", role);
        model.addAttribute("loginTime", LocalDateTime.now());

        if (platform.contains("Windows")) return "admin.html";
        else
            return "admin_mobile";

    }

    @GetMapping("/owner")
    public String owner(HttpServletRequest httpServletRequest, Model model){
        return "owner";
    }

    @GetMapping("/tenant")
    public String tenant(HttpServletRequest httpServletRequest, Model model){
        return "tenant";
    }


}
