package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "InventoryLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logID")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchID", nullable = false)
    private DrugBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(name = "actionType", nullable = false, length = 20)
    private String actionType;

    @Column(name = "quantityChange", nullable = false)
    private Integer quantityChange;

    @Column(name = "quantityBefore", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantityAfter", nullable = false)
    private Integer quantityAfter;

    @Column(name = "referenceID")
    private Integer referenceId;

    @Column(name = "referenceType", length = 50)
    private String referenceType;

    @CreationTimestamp
    @Column(name = "performedAt", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @Column(name = "notes", length = 500)
    private String notes;
}
