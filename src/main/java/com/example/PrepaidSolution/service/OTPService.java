package com.example.PrepaidSolution.service;

import com.example.PrepaidSolution.dto.OTPData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OTPService {

    public static final int OTP_TTL_SECONDS = 300;
    public static final int RESEND_PROMPT_DELAY_SECONDS = 30;
    public static final int MAX_SEND_ATTEMPTS = 2;

    private final RedisTemplate<String, Object> redisTemplate;

    public OtpGenerationResult generateOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        OTPData existingData = getOtpData(normalizedEmail);

        if (existingData != null && !existingData.isExpired() && existingData.getSendCount() >= MAX_SEND_ATTEMPTS) {
            return new OtpGenerationResult(
                    false,
                    null,
                    existingData.getSendCount(),
                    "Something is wrong. Please try again later."
            );
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        int sendCount = (existingData != null && !existingData.isExpired())
                ? existingData.getSendCount() + 1
                : 1;

        OTPData data = new OTPData();
        data.setOtp(otp);
        data.setSendCount(sendCount);
        data.setExpiryTime(System.currentTimeMillis() + OTP_TTL_SECONDS * 1000L);

        redisTemplate.opsForValue().set(buildKey(normalizedEmail), data, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        return new OtpGenerationResult(true, otp, sendCount, "OTP sent successfully.");
    }

    public boolean validateOtp(String email, String otp) {
        OTPData data = getOtpData(normalizeEmail(email));
        if (data == null) return false;

        if (data.isExpired()) return false;

        return data.getOtp().equals(otp);
    }

    public void clearOtp(String email) {
        redisTemplate.delete(buildKey(normalizeEmail(email)));
    }

    private String buildKey(String email) {
        return "otp:" + email;
    }

    private OTPData getOtpData(String email) {
        Object obj = redisTemplate.opsForValue().get(buildKey(email));
        if (!(obj instanceof OTPData data)) {
            return null;
        }
        return data;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record OtpGenerationResult(
            boolean allowed,
            String otp,
            int sendCount,
            String message
    ) {
    }
}
