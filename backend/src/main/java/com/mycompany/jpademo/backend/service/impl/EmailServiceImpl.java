package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${mail.from-name}")
    private String fromName;

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        buildAndSendEmail(to, subject, body);
    }

    @Override
    public void sendCreateDoctorAccountEmail(String to, String subject, String fullName, String username, String password) {
        String body = EmailUtil.buildCreateDoctorAccountTemplate(fullName, username, password);
        buildAndSendEmail(to, subject, body);
    }

    private void buildAndSendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(fromEmail, MimeUtility.encodeText(fromName, "UTF-8", "B"));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email");
        }
    }
}
