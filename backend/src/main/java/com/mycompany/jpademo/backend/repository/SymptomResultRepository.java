package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SymptomResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SymptomResultRepository extends JpaRepository<SymptomResult, Integer> {
    Optional<SymptomResult> findByDiagnosisSession_SessionId(Integer sessionId);

    // Alias method cho compatibility với các service khác
    default Optional<SymptomResult> findByDiagnosisSessionSessionId(Integer sessionId) {
        return findByDiagnosisSession_SessionId(sessionId);
    }
}
