package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DiseaseType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diseaseTypeID")
    private Integer diseaseTypeId;

    @Column(name = "name", nullable = false)
    private String name;
}
