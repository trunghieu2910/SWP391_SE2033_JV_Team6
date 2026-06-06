package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Column(name = "status")
    private SymptomResultStatus status;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "menopauseStatus", nullable = false)
    private String menopauseStatus;

    @Column(name = "symptomDuration", nullable = false)
    private String symptomDuration;

    @Column(name = "symptomProgressing", nullable = false)
    private Boolean symptomProgressing;

    @OneToMany(
            mappedBy = "symptomResult",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SymptomDetails> symptomDetails = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false, unique = true)
    private DiagnosisSession diagnosisSession;

}