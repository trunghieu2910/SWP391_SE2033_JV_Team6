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
    @Column(name = "reviewID")
    private Integer reviewId;

    @Column(name = "treatmentPlan", nullable = false)
    private String treatmentPlan;

    @Column(name = "doctorAdvice")
    private String doctorAdvice;

    @Column(name = "note")
    private String note;

    @CreationTimestamp
    @Column(name = "reviewedAt", nullable = false, updatable = false)
    private LocalDateTime reviewedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false, unique = true)
    private DiagnosisSession diagnosisSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diseaseTypeID", nullable = false)
    private DiseaseType diseaseType;
}
