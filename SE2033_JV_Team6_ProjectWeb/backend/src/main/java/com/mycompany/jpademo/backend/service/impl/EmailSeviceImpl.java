package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSeviceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${mail.from-name}")
    private String fromName;

    @Async
    @Override
    public void sendBanEmail(String to, String subject) {
        sendEmail(to, subject, EmailUtil.buildBanAccountTemplate());
    }

    @Override
    public void senUnbanEmail(String to, String subject) {
        sendEmail(to, subject, EmailUtil.buildDoctorApprovedTemplate());
    }

    @Override
    public void senApproveEmail(String to, String subject) {
        sendEmail(to, subject, EmailUtil.buildDoctorApprovedTemplate());
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(fromEmail, fromName);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email");
        }
    }
}
