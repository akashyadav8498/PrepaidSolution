package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.Users;
import com.example.PrepaidSolution.repository.UsersRepository;
import com.example.PrepaidSolution.service.EmailService;
import com.example.PrepaidSolution.service.OTPService;
import com.example.PrepaidSolution.util.Utility;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController {

    private final UsersRepository usersRepository;
    private final OTPService otpService;
    private final EmailService emailService;

    @PostMapping("/addUser")
    public Users createUser(@RequestBody Users user) {
        user.setPassword(Utility.passwordEncoder.encode(user.getPassword()));
        return usersRepository.save(user);
    }

    @PostMapping("/sendOTP")
    public ResponseEntity<?> sendOTP(@RequestBody Map<String, String> req) {
        String email = normalizeEmail(req.get("email"));

        if (email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }

        if (usersRepository.findByEmailIgnoreCase(email).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No user found for this email."));
        }

        OTPService.OtpGenerationResult result = otpService.generateOtp(email);
        if (!result.allowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "message", result.message(),
                    "sendCount", result.sendCount(),
                    "maxAttempts", OTPService.MAX_SEND_ATTEMPTS,
                    "resendPromptDelaySeconds", OTPService.RESEND_PROMPT_DELAY_SECONDS
            ));
        }

        emailService.sendOTP(email, result.otp());

        return ResponseEntity.ok(Map.of(
                "message", result.message(),
                "sendCount", result.sendCount(),
                "maxAttempts", OTPService.MAX_SEND_ATTEMPTS,
                "resendPromptDelaySeconds", OTPService.RESEND_PROMPT_DELAY_SECONDS
        ));
    }

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    @PostMapping("/verifyOTP")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> req,
                                       HttpServletRequest httpServletRequest,
                                       HttpServletResponse httpServletResponse) {
        String email = normalizeEmail(req.get("email"));
        String otp = req.getOrDefault("otp", "").trim();

        if (email.isBlank() || otp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required."));
        }

        Users user = usersRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No user found for this email."));
        }

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired OTP."));
        }

        String authority = "ROLE_" + user.getRole().name();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority(authority))
                );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, httpServletRequest, httpServletResponse);

        HttpSession httpSession = httpServletRequest.getSession(true);
        httpSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );

        String role = user.getRole().name();

        httpSession.setAttribute("email", user.getEmail());
        httpSession.setAttribute("role", role);

        otpService.clearOtp(email);

        int httpStatusCode = 200;
        String message = "Login successful.";
        String redirectUrl = "";

        if(role.equalsIgnoreCase("owner")) redirectUrl = "/owner";
        else if(role.equalsIgnoreCase("tenant")) redirectUrl = "/tenant";
        else {
            httpStatusCode = 403;
            message = "This page is not accessible to admin.";
        }

        return new ResponseEntity<>(
                Map.of(
                        "message", message,
                        "redirectUrl", redirectUrl
                ),
                HttpStatusCode.valueOf(httpStatusCode)
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
