package com.mycompany.jpademo.backend.service.interfaces;

public interface EmailService {

    void sendOtp(String toEmail, String recipientName, String otp);
}
