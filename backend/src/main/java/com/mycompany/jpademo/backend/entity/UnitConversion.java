package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "UnitConversion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitConversion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversionID")
    private Integer conversionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drugID", nullable = false)
    private Drug drug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "largeUnitID", nullable = false)
    private Unit largeUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "smallUnitID", nullable = false)
    private Unit smallUnit;

    @Column(name = "conversionQuantity", nullable = false)
    private Integer conversionQuantity;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
