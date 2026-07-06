package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DiagnosisSession")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sessionID")
    private Integer sessionId;

    @Positive
    @Column(name = "weight", nullable = true)
    private Double weight;

    @Positive
    @Column(name = "height", nullable = true)
    private Double height;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private DiagnosisSessionStatus status = DiagnosisSessionStatus.PROCESSING;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @OneToOne(mappedBy = "diagnosisSession", fetch = FetchType.LAZY)
    private SymptomResult symptomResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "clinicalInputMode", length = 20)
    private ClinicalInputMode clinicalInputMode;

    @OneToMany(mappedBy = "diagnosisSession")
    private List<LabResult> labResults;

    @OneToMany(mappedBy = "diagnosisSession", fetch = FetchType.LAZY)
    private List<MedicalImage> medicalImages;

    @OneToOne(mappedBy = "diagnosisSession", fetch = FetchType.LAZY)
    private Review review;

    @Column(name = "isShared")
    @Builder.Default
    private Boolean isShared = false;
}