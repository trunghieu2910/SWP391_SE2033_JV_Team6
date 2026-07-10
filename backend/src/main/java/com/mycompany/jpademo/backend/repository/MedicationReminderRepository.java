package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.MedicationReminder;
import com.mycompany.jpademo.backend.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Integer> {

    List<MedicationReminder> findByPatientPatientIdOrderByScheduledAtAsc(Integer patientId);

    List<MedicationReminder> findByPatientPatientIdAndStatusOrderByScheduledAtAsc(Integer patientId, ReminderStatus status);

    List<MedicationReminder> findByStatusOrderByScheduledAtAsc(ReminderStatus status);

    Optional<MedicationReminder> findByReminderIdAndPatientPatientId(Integer reminderId, Integer patientId);
}
