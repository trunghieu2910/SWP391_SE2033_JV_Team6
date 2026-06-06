package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "SymptomResult")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symptomResultID")
    private Integer symptomResultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private SymptomResultStatus status = SymptomResultStatus.PENDING;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false, unique = true)
    private DiagnosisSession diagnosisSession;

    @Column(name = "menopauseStatus", length = 50)
    private String menopauseStatus;

    @Column(name = "symptomDuration", length = 50)
    private String symptomDuration;

    @Column(name = "symptomProgressing")
    private Boolean symptomProgressing;

    @OneToMany(mappedBy = "symptomResult", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SymptomDetails> symptomDetailsList;
}
