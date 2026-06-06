package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LabResultParameter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResultParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "labResultParameterID")
    private Integer labResultParameterId;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labResultID", nullable = false)
    private LabResult labResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameterID", nullable = false)
    private Parameter parameter;
}