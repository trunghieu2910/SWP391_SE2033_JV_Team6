package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @Column(name = "verdict", nullable = false, length = 50)
    private String verdict;

    @Column(name = "finalDiagnosis", nullable = false)
    private String finalDiagnosis;

    @Column(name = "icd10Code", nullable = false)
    private String icd10Code;

    @Column(name = "treatmentPlan", nullable = false)
    private String treatmentPlan;

    @Column(name = "note")
    private String note;

    @CreationTimestamp
    @Column(name = "reviewedAt", nullable = false, updatable = false)
    private LocalDateTime reviewAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false, unique = true)
    private DiagnosisSession diagnosisSession;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;
}
