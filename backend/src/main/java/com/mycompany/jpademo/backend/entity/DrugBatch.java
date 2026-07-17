package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DrugBatch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batchID")
    private Integer batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drugID", nullable = false)
    private Drug drug;

    @Column(name = "batchNumber", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "manufactureDate", nullable = false)
    private LocalDate manufactureDate;

    @Column(name = "expiryDate", nullable = false)
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unitID", nullable = false)
    private Unit unit;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "importPrice")
    private BigDecimal importPrice;

    @Column(name = "supplier", length = 200)
    private String supplier;

    @CreationTimestamp
    @Column(name = "importDate", nullable = false, updatable = false)
    private LocalDateTime importDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importedBy", nullable = false)
    private User importedByUser;

    @Column(name = "status")
    private Byte status;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionDetail> prescriptionDetails;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryLog> logs;
}
