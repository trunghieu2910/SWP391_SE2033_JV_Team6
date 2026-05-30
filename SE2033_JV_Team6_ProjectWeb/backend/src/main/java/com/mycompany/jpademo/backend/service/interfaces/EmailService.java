package com.mycompany.jpademo.backend.service.interfaces;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void sendCreateDoctorAccountEmail(String to, String subject, String fullName, String username, String password);
}
