package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MedicalImage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer medicalImageID;

    @Column(name = "imageType", nullable = false)
    private String imageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "medicalImage")
    private List<MedicalImageDetails> medicalImageDetailsList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false)
    private DiagnosisSession diagnosisSession;
}