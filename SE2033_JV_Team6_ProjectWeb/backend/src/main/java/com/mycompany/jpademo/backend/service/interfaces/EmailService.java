package com.mycompany.jpademo.backend.service.interfaces;

public interface EmailService {
    void sendBanEmail(String to, String subject);

    void senUnbanEmail(String to, String subject);

    void senApproveEmail(String to, String subject);
}
