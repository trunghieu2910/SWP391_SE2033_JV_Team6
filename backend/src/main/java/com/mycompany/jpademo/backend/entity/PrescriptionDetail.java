package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PrescriptionDetail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detailID")
    private Integer detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescriptionID", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drugID", nullable = false)
    private Drug drug;

    @Column(name = "dosePerTime", nullable = false)
    private BigDecimal dosePerTime;

    @Column(name = "timesPerDay", nullable = false)
    private Integer timesPerDay;

    @Column(name = "daysOfTreatment", nullable = false)
    private Integer daysOfTreatment;

    @Column(name = "quantityPrescribed", nullable = false)
    private Integer quantityPrescribed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchID")
    private DrugBatch batch;

    @Column(name = "quantityDispensed")
    private Integer quantityDispensed;

    @Column(name = "actualExpiryDate")
    private LocalDate actualExpiryDate;

    @Column(name = "dispenseUnit", length = 10)
    private String dispenseUnit;

    @Column(name = "instruction", length = 500)
    private String instruction;

    @Column(name = "dispensedAt")
    private LocalDateTime dispensedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensedBy")
    private User dispensedByUser;

    @Column(name = "notes", length = 200)
    private String notes;
}
