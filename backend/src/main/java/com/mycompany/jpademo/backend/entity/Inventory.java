package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventoryID")
    private Integer inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchID", nullable = false)
    private DrugBatch batch;

    @Column(name = "quantityInStock", nullable = false)
    private Integer quantityInStock;

    @UpdateTimestamp
    @Column(name = "lastUpdated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "status")
    private Byte status;
}
