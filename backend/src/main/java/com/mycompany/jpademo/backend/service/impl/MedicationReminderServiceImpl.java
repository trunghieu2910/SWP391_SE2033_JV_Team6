
package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.MedicationReminderRequest;
import com.mycompany.jpademo.backend.dto.response.MedicationReminderResponse;
import com.mycompany.jpademo.backend.entity.MedicationReminder;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.enums.ReminderStatus;
import com.mycompany.jpademo.backend.repository.MedicationReminderRepository;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.service.interfaces.MedicationReminderService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicationReminderServiceImpl implements MedicationReminderService {

    private final MedicationReminderRepository reminderRepository;
    private final PatientRepository patientRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    //Nhận lệnh từ controller và ghi xuống database
    public MedicationReminderResponse createReminder(Integer patientId, MedicationReminderRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân."));

        MedicationReminder reminder = MedicationReminder.builder()
                .patient(patient)
                .note(request.getNote())
                .scheduledAt(LocalDateTime.of(LocalDateTime.now().toLocalDate(), request.getScheduledTime()))
                .status(ReminderStatus.PENDING)
                .build();

        MedicationReminder saved = reminderRepository.save(reminder);
        return mapToResponse(saved);
    }

    @Override
    public List<MedicationReminderResponse> getUpcomingReminders(Integer patientId, int limit) {
        return reminderRepository.findByPatientPatientIdAndStatusOrderByScheduledAtAsc(patientId, ReminderStatus.PENDING).stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicationReminderResponse> getAllReminders(Integer patientId) {
        return reminderRepository.findByPatientPatientIdOrderByScheduledAtAsc(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicationReminderResponse updateReminder(Integer patientId, Integer reminderId, MedicationReminderRequest request) {
        MedicationReminder reminder = reminderRepository.findByReminderIdAndPatientPatientId(reminderId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhắc uống thuốc."));

        reminder.setNote(request.getNote());
        reminder.setScheduledAt(LocalDateTime.of(LocalDate.now(), request.getScheduledTime()));

        MedicationReminder updated = reminderRepository.save(reminder);
        return mapToResponse(updated);
    }

    @Override
    public MedicationReminderResponse getReminder(Integer patientId, Integer reminderId) {
        MedicationReminder reminder = reminderRepository.findByReminderIdAndPatientPatientId(reminderId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhắc uống thuốc."));
        return mapToResponse(reminder);
    }

    @Override
    @Transactional
    public void deleteReminder(Integer patientId, Integer reminderId) {
        MedicationReminder reminder = reminderRepository.findByReminderIdAndPatientPatientId(reminderId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhắc uống thuốc."));
        reminderRepository.delete(reminder);
    }

    private MedicationReminderResponse mapToResponse(MedicationReminder reminder) {
        return MedicationReminderResponse.builder()
                .reminderId(reminder.getReminderId())
                .note(reminder.getNote())
                .scheduledAt(reminder.getScheduledAt())
                .scheduledTime(reminder.getScheduledAt() != null ? reminder.getScheduledAt().toLocalTime() : null)
                .createdAt(reminder.getCreatedAt())
                .sentAt(reminder.getSentAt())
                .status(reminder.getStatus())
                .build();
    }
}

