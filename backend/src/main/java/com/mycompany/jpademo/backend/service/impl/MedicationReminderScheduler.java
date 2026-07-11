
package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.entity.MedicationReminder;
import com.mycompany.jpademo.backend.enums.ReminderStatus;
import com.mycompany.jpademo.backend.repository.MedicationReminderRepository;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationReminderScheduler {

    private final MedicationReminderRepository reminderRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelayString = "PT1M")
    @Transactional
    public void processScheduledReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        List<MedicationReminder> reminders = reminderRepository.findByStatusOrderByScheduledAtAsc(ReminderStatus.PENDING);
        for (MedicationReminder reminder : reminders) {
            try {
                if (shouldSendReminder(reminder, now, currentTime)) {
                    String recipient = reminder.getPatient().getUser().getEmail();
                    String subject = "Nhắc uống thuốc";
                    String body = buildReminderBody(reminder);
                    emailService.sendEmail(recipient, subject, body);
                    reminder.setSentAt(now);
                    reminderRepository.save(reminder);
                }
            } catch (Exception ex) {
                log.error("Không gửi được email nhắc uống thuốc cho reminder {}: {}", reminder.getReminderId(), ex.getMessage());
            }
        }
    }

    private boolean shouldSendReminder(MedicationReminder reminder, LocalDateTime now, LocalTime currentTime) {
        if (reminder.getScheduledAt() == null) {
            return false;
        }

        LocalTime scheduledTime = reminder.getScheduledAt().toLocalTime();
        if (reminder.getSentAt() != null && reminder.getSentAt().toLocalDate().isEqual(now.toLocalDate())) {
            return false;
        }

        long minutesBetween = Duration.between(scheduledTime, currentTime).toMinutes();
        return minutesBetween >= 0 && minutesBetween < 1;
    }

    private String buildReminderBody(MedicationReminder reminder) {
        return "<html><body>"
                + "<div style=\"font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 30px;\">"
                + "<h2 style=\"color: #2563eb;\">Nhắc uống thuốc</h2>"
                + "<p>Xin chào <strong>" + escapeHtml(reminder.getPatient().getUser().getFullName()) + "</strong>,</p>"
                + "<p>Đây là lời nhắc uống thuốc theo lịch bạn đã tạo:</p>"
                + "<p><strong>Ghi chú:</strong> " + escapeHtml(reminder.getNote()) + "</p>"
                + "<p><strong>Thời gian nhắc:</strong> " + reminder.getScheduledAt().toString().replace('T', ' ') + "</p>"
                + "<br><p>Hãy kiểm tra và thực hiện uống thuốc đúng giờ.</p>"
                + "<br><p>Trân trọng,<br>Hệ thống MedAI</p>"
                + "</div>"
                + "</div>"
                + "</body></html>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

