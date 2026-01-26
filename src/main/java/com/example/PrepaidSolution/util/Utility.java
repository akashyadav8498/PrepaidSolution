package com.example.PrepaidSolution.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Random;

public class Utility {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@#$%^&*()-_+=<>?";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + DIGITS + SPECIALS;
    private static final SecureRandom random = new SecureRandom();
    public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replaceAll("\\s+", ""); // Remove any spaces
        int len = s.length();
        if (len % 2 != 0) throw new IllegalArgumentException("Invalid hex string");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String generateUsername(String fullName, String mobile, int maxLength) {

        StringBuilder initials = new StringBuilder();
        String[] names = fullName.trim().split("\\s+");
        for (String name : names) {
            if (!name.isEmpty()) {
                initials.append(Character.toUpperCase(name.charAt(0)));
            }
        }

        String mobileDigits = mobile.length() >= 4 ? mobile.substring(mobile.length() - 4) : mobile;

        StringBuilder base = new StringBuilder(initials + mobileDigits);

        Random random = new Random();
        while (base.length() < maxLength) {
            base.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }

        if (base.length() > maxLength) {
            base = new StringBuilder(base.substring(0, maxLength));
        }

        return base.toString();
    }

    public static String generatePassword(int length) {
        if (length < 6) length = 6; // Minimum safe length
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(idx));
        }
        return password.toString();
    }
}
