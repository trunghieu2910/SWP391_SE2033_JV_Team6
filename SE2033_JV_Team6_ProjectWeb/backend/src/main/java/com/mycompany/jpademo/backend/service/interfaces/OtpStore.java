package com.mycompany.jpademo.backend.service.interfaces;

public interface OtpStore {

    String generateOtp(String key);

    boolean validateOtp(String key, String otp);

    void removeOtp(String key);
}
