package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.exception.EmailSendingException;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String name, String otp) {
        try{
            MimeMessage message  = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setTo(toEmail);
            helper.setSubject("Xác thực OTP");

            String htmlContent = EmailUtil.buildOtpEmailTemplate(name, otp);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new EmailSendingException();
        }
    }
}
