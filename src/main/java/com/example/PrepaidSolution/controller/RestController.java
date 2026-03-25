package com.example.PrepaidSolution.controller;

import com.example.PrepaidSolution.model.Users;
import com.example.PrepaidSolution.repository.UsersRepository;
import com.example.PrepaidSolution.service.EmailService;
import com.example.PrepaidSolution.service.OTPService;
import com.example.PrepaidSolution.util.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController {

    private final UsersRepository usersRepository;

    private final OTPService otpService;

    private EmailService emailService;

    @PostMapping("/addUser")
    public Users createUser(@RequestBody Users user) {
        user.setPassword(Utility.passwordEncoder.encode(user.getPassword()));
        return usersRepository.save(user);
    }


    @PostMapping("/sendOTP")
    public ResponseEntity<?> sendOTP(@RequestBody Map<String, String> req) {

        String email = req.get("email");

        String otp = otpService.generateOtp(email);
        emailService.sendOTP(email, otp);

        return ResponseEntity.ok("OTP sent");
    }

    @PostMapping("/verifyOTP")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String otp = req.get("otp");

        if (otpService.validateOtp(email, otp)) {
            return ResponseEntity.ok("Login Success");
        }

        return ResponseEntity.status(401).body("Invalid OTP");
    }
}
