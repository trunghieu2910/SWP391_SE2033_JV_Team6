package com.mycompany.jpademo.backend.service.interfaces;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void sendOtpEmail(String to, String fullName, String otp);

    void sendPasswordEmail(String toEmail, String fullName, String rawPassword);
}
