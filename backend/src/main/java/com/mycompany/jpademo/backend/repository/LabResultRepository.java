package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Integer> {
    List<LabResult> findByDiagnosisSessionSessionId(Integer sessionId);

    @Query("SELECT lr FROM LabResult lr " +
            "LEFT JOIN FETCH lr.labResultParameters lrp " +
            "LEFT JOIN FETCH lrp.parameter p " +
            "WHERE lr.diagnosisSession.sessionId = :sessionId")
    List<LabResult> findBySessionIdWithParameters(@Param("sessionId") Integer sessionId);
}