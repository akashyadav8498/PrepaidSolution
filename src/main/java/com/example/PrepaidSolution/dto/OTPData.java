package com.example.PrepaidSolution.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPData {
    private String otp;
    private long expiryTime;

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
