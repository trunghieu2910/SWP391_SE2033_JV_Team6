package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.DrugStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Drug")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drugID")
    private Integer drugId;

    @Column(name = "drugCode", nullable = false, unique = true, length = 20)
    private String drugCode;

    @Column(name = "drugName", nullable = false, length = 200)
    private String drugName;

    @Column(name = "strength", length = 50)
    private String strength;

    @Column(name = "strengthUnit", nullable = false, length = 10)
    private String strengthUnit;

    @Column(name = "dosageForm", nullable = false, length = 50)
    private String dosageForm;

    @Column(name = "routeOfAdministration", nullable = false, length = 50)
    private String routeOfAdministration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subCategoryID", nullable = false)
    private DrugSubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baseUnitID", nullable = false)
    private Unit baseUnit;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "countryOfOrigin", length = 50)
    private String countryOfOrigin;

    @Column(name = "storageCondition", length = 200)
    private String storageCondition;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DrugStatus status;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private User createdByUser;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updatedBy")
    private User updatedByUser;



    @OneToMany(mappedBy = "drug", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitConversion> conversions;

    @OneToMany(mappedBy = "drug", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DrugBatch> batches;

    @OneToMany(mappedBy = "drug", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionDetail> prescriptionDetails;
}
