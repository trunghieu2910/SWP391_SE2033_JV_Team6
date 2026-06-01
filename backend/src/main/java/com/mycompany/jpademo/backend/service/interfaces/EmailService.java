package com.mycompany.jpademo.backend.service.interfaces;

public interface EmailService {

    void sendOtpEmail(String toEmail, String name, String otp);
}
