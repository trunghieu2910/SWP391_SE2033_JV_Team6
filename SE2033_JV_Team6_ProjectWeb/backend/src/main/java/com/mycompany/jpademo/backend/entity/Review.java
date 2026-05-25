package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "Review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewID")
    private Integer reviewID;

    @OneToOne
    @JoinColumn(name = "sessionID", nullable = false)
    private DiagnosisSession session;

    @Column(name = "verdict", length = 20)
    private String verdict;

    @Column(name = "finalDiagnosis", columnDefinition = "NVARCHAR(255)")
    private String finalDiagnosis;

    @Column(name = "icd10Code", length = 10)
    private String icd10Code;

    @Column(name = "treatmentPlan", columnDefinition = "NVARCHAR(MAX)")
    private String treatmentPlan;

    @Column(name = "doctorAdvice", columnDefinition = "NVARCHAR(MAX)")
    private String doctorAdvice;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reviewedAt")
    private Date reviewedAt = new Date();
}