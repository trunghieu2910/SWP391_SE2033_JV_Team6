package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DrugSubCategory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugSubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subCategoryID")
    private Integer subCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryID", nullable = false)
    private DrugCategory category;

    @Column(name = "subCategoryName", nullable = false, unique = true, length = 100)
    private String subCategoryName;

    @Column(name = "priorityLevel")
    private Byte priorityLevel;

    @Column(name = "requireSpecialPrescription")
    private Boolean requireSpecialPrescription;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Drug> drugs;
}
