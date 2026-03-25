package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.dto.OTPData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OTPService {

    private final RedisTemplate<String, Object> redisTemplate;

    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        OTPData data = new OTPData();
        data.setOtp(otp);
        data.setExpiryTime(System.currentTimeMillis() + 5 * 60 * 1000);

        redisTemplate.opsForValue().set(email, data, 5, TimeUnit.MINUTES);

        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        Object obj = redisTemplate.opsForValue().get(email);

        if (obj == null) return false;

        OTPData data = (OTPData) obj;

        if (data.isExpired()) return false;

        return data.getOtp().equals(otp);
    }
}
