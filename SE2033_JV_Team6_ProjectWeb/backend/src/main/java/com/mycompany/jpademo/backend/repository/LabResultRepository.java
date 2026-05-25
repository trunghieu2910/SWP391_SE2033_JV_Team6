package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Integer> {
    List<LabResult> findByDiagnosisSessionSessionID(Integer sessionID);
}