package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SymptomResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SymptomResultRepository extends JpaRepository<SymptomResult, Integer> {
    Optional<SymptomResult> findByDiagnosisSession_SessionId(Integer sessionId);

    default Optional<SymptomResult> findByDiagnosisSessionSessionId(Integer sessionId) {
        return findByDiagnosisSession_SessionId(sessionId);
    }

    @Query("SELECT sr FROM SymptomResult sr " +
            "LEFT JOIN FETCH sr.symptomDetails sd " +
            "LEFT JOIN FETCH sd.symptom s " +
            "WHERE sr.diagnosisSession.sessionId = :sessionId")
    Optional<SymptomResult> findBySessionIdWithDetails(@Param("sessionId") Integer sessionId);
}
