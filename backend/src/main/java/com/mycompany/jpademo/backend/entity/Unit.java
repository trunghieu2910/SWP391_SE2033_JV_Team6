package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unitID")
    private Integer unitId;

    @Column(name = "unitName", nullable = false, unique = true, length = 20)
    private String unitName;

    @Column(name = "description", length = 100)
    private String description;

    @OneToMany(mappedBy = "largeUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitConversion> largeUnitConversions;

    @OneToMany(mappedBy = "smallUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitConversion> smallUnitConversions;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DrugBatch> batches;
}
