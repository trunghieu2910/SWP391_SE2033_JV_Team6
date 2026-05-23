package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.MedicalImageStatus;
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
    private Integer medicalImageId;

    @Column(name = "imageType", nullable = false)
    private String imageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private MedicalImageStatus status = MedicalImageStatus.PENDING;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "medicalImage")
    private List<MedicalImageDetails> medicalImageDetailsList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionID", nullable = false)
    private DiagnosisSession diagnosisSession;
}