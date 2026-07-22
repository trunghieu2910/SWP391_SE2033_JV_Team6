package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "MedicalImageDetails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalImageDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageID")
    private Integer imageId;

    @Column(name = "imageUrl", nullable = false)
    private String imageUrl;

    @Column(name = "aiImageUrl")
    private String aiImageUrl;

    @Column(name = "confidenceScore")
    private Double confidenceScore;

    @Column(name = "ultrasoundConclusion", columnDefinition = "NVARCHAR(MAX)")
    private String ultrasoundConclusion;

    @Column(name = "imgResultConclusion")
    private String imgResultConclusion;

    @CreationTimestamp
    @Column(name = "uploadedAt", nullable = false, updatable = false)
    private java.time.LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicalImageID", nullable = false)
    private MedicalImage medicalImage;
}