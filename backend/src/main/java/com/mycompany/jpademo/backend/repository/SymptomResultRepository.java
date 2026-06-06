package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SymptomResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SymptomResultRepository extends JpaRepository<SymptomResult, Integer> {
    Optional<SymptomResult> findByDiagnosisSessionSessionId(Integer sessionId);
}
