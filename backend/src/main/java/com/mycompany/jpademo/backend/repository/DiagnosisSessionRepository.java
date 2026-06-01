package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Integer> {
}
