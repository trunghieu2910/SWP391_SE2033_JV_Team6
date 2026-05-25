package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SymptomDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SymptomDetailsRepository extends JpaRepository<SymptomDetails, Integer> {
    List<SymptomDetails> findByDiagnosisSessionSessionID(Integer sessionID);
}