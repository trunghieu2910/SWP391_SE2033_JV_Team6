package com.mycompany.jpademo.backend.service.interfaces;

public interface EmailService {
    void sendEmail(String to, String subject, String name);
}
