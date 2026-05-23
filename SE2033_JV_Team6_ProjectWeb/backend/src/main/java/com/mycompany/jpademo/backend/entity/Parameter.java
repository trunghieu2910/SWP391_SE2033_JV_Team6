package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    private Integer parameterId;

    @Column(name = "parameterName", unique = true, length = 100, nullable = false)
    private String parameterName;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @OneToMany(mappedBy = "parameter")
    private List<LabResultParameter> labResultParameters;
}