package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Symptom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Symptom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symptomID")
    private Integer symptomId;

    @Column(name = "symptomName", unique = true, nullable = false, length = 100)
    private String symptomName;

    @OneToMany(mappedBy = "symptom")
    private List<SymptomDetails> symptomDetails;
}
