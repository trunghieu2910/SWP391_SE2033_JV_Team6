package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.service.interfaces.OtpStore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOtpStore implements OtpStore {

    private static final int OTP_LENGTH = 6;
    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public String generateOtp(String key) {
        String otp = generateNumericOtp();
        Instant expiresAt = Instant.now().plus(2, ChronoUnit.MINUTES);
        otpCache.put(key, new OtpEntry(otp, expiresAt));
        return otp;
    }

    @Override
    public boolean validateOtp(String key, String otp) {
        OtpEntry entry = otpCache.get(key);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.getExpiresAt())) {
            otpCache.remove(key);
            return false;
        }
        boolean valid = entry.getOtp().equals(otp);
        if (valid) {
            otpCache.remove(key);
        }
        return valid;
    }

    @Override
    public void removeOtp(String key) {
        otpCache.remove(key);
    }

    private String generateNumericOtp() {
        StringBuilder builder = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private static class OtpEntry {
        private final String otp;
        private final Instant expiresAt;

        public OtpEntry(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        public String getOtp() {
            return otp;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }
    }
}
