package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.LabResultStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "LabResult")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "labResultID")
    private Integer labResultId;

    @Column(name = "testType", nullable = false, length = 100)
    private String testType;

    @Column(name = "status", length = 50)
    @Builder.Default
    private LabResultStatus status = LabResultStatus.PENDING;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "labResult")
    private List<LabResultParameter> labResultParameters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false)
    private DiagnosisSession diagnosisSession;
}