package com.example.PrepaidSolution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OTPData {
    private String otp;
    private long expiryTime;
    private int sendCount;

    @JsonIgnore
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
