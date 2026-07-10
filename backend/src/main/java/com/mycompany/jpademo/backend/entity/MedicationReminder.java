package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MedicationReminder")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminderID")
    private Integer reminderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    @Column(name = "note", length = 500, nullable = false)
    private String note;

    @Column(name = "scheduledAt", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sentAt")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReminderStatus status;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReminderStatus.PENDING;
        }
    }
}
