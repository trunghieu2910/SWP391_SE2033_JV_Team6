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

    @Override
    public void sendEmail(String to, String subject, String body) {
        buildAndSendEmail(to, subject, body);
    }

    @Override
    public void sendOtpEmail(String to, String fullName, String otp) {
        String subject = "Mã OTP đặt lại mật khẩu";
        String body = EmailUtil.buildOtpEmailTemplate(fullName, otp);
        buildAndSendEmail(to, subject, body);
    }

    @Override
    public void sendRegistrationOtpEmail(String to, String fullName, String otp) {
        String subject = "Mã OTP xác nhận đăng ký";
        String body = EmailUtil.buildRegistrationOtpEmailTemplate(fullName, otp);
        buildAndSendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordEmail(String toEmail, String fullName, String rawPassword) {
        String subject = "[MedicalDiagnosis] Thông tin tài khoản của bạn";
        String body = EmailUtil.buildPasswordEmailTemplate(fullName, rawPassword);
        buildAndSendEmail(toEmail, subject, body);
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
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email");
        }
    }
}
