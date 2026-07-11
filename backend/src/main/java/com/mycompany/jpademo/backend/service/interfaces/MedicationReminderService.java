
package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.MedicationReminderRequest;
import com.mycompany.jpademo.backend.dto.response.MedicationReminderResponse;

import java.util.List;

public interface MedicationReminderService {

    MedicationReminderResponse createReminder(Integer patientId, MedicationReminderRequest request);

    MedicationReminderResponse getReminder(Integer patientId, Integer reminderId);

    MedicationReminderResponse updateReminder(Integer patientId, Integer reminderId, MedicationReminderRequest request);

    List<MedicationReminderResponse> getUpcomingReminders(Integer patientId, int limit);

    List<MedicationReminderResponse> getAllReminders(Integer patientId);

    void deleteReminder(Integer patientId, Integer reminderId);
}

