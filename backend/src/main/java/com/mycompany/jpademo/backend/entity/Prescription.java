package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Prescription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescriptionID")
    private Integer prescriptionId;

    @Column(name = "prescriptionCode", nullable = false, unique = true, length = 20)
    private String prescriptionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false)
    private DiagnosisSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorID", nullable = false)
    private User doctor;

    @Column(name = "diagnosis", length = 500)
    private String diagnosis;

    @Column(name = "treatmentCycle", length = 50)
    private String treatmentCycle;

    @CreationTimestamp
    @Column(name = "prescriptionDate", nullable = false, updatable = false)
    private LocalDateTime prescriptionDate;

    @Column(name = "status")
    private Byte status;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionDetail> details;
}
