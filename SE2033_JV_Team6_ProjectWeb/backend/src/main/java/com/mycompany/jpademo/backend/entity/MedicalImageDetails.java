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
    private Integer imageID;

    @Column(name = "imageUrl", unique = true, nullable = false)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "uploadedAt", nullable = false, updatable = false)
    private java.time.LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicalImageID", nullable = false)
    private MedicalImage medicalImage;
}