package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Integer> {
    List<Symptom> findBySymptomNameIn(Collection<String> symptomNames);
}