package com.mycompany.jpademo.backend.entity;

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
    private Integer sessionID;

    @NotNull
    @Positive
    @Column(name = "weight", nullable = false)
    private Double  weight;

    @NotNull
    @Positive
    @Column(name = "height", nullable = false)
    private Double  height;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private String status;

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

    @OneToMany(mappedBy = "diagnosisSession")
    private List<MedicalImage> medicalImages;
}
