package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Shared, system-wide catalog of measurable lab parameters
 * (e.g. "WBC", "HPV Type 16") with their unit of measurement.
 * New entries can be auto-created by the LIS integration flow when
 * an incoming result references a parameter name not yet in the
 * catalog.
 */
@Entity
@Table(name = "Parameter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parameterID")
    private Integer parameterId;

    @Column(name = "parameterName", unique = true, length = 100, nullable = false)
    private String parameterName;

    @Column(name = "unit", nullable = true, length = 50)
    private String unit;

    @OneToMany(mappedBy = "parameter")
    private List<LabResultParameter> labResultParameters;
}