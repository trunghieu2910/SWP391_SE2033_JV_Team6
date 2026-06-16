package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SymptomDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SymptomDetailsRepository extends JpaRepository<SymptomDetails, Integer> {
    // Query theo cấu trúc DB mới: SymptomDetails -> SymptomResult -> DiagnosisSession
    List<SymptomDetails> findBySymptomResult_DiagnosisSession_SessionId(Integer sessionId);
}