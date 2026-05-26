package com.mycompany.jpademo.backend.service.interfaces;

public interface EmailService {

    void sendOtpEmail(String toEmail, String name, String otp);

    void sendBanEmail(String to, String subject, String name);

    void sendUnbanEmail(String to, String subject, String name);

    void sendApproveEmail(String to, String subject, String name);

    void sendRejectEmail(String to, String subject, String name);
}
