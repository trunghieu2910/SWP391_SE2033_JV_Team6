package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Positive
    @Column(name = "weight", nullable = false)
    private Double weight;

    @NotNull
    @Positive
    @Column(name = "height", nullable = false)
    private Double height;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private DiagnosisSessionStatus status = DiagnosisSessionStatus.PENDING;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @OneToMany(mappedBy = "diagnosisSession")
    private List<SymptomDetails> symptomDetailsList;

    @OneToMany(mappedBy = "diagnosisSession")
    private List<LabResult> labResults;

    @OneToMany(mappedBy = "diagnosisSession", fetch = FetchType.LAZY)
    private List<MedicalImage> medicalImages;

    @OneToOne(mappedBy = "diagnosisSession", fetch = FetchType.LAZY)
    private Review review;
}
