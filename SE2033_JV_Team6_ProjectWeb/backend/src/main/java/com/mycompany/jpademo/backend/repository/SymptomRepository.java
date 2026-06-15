package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Integer> {

}