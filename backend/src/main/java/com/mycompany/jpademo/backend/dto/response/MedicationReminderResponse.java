package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.ReminderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationReminderResponse {
    private Integer reminderId;
    private String note;
    private LocalDateTime scheduledAt;
    private LocalTime scheduledTime;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private ReminderStatus status;
}
