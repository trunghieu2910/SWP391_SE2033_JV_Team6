package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.MedicalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalImageRepository extends JpaRepository<MedicalImage, Integer> {
    List<MedicalImage> findByDiagnosisSessionSessionId(Integer sessionId);

    default List<MedicalImage> findByDiagnosisSession_SessionId(Integer sessionId) {
        return findByDiagnosisSessionSessionId(sessionId);
    }

    @Query("SELECT mi FROM MedicalImage mi " +
            "LEFT JOIN FETCH mi.medicalImageDetailsList mid " +
            "WHERE mi.diagnosisSession.sessionId = :sessionId")
    List<MedicalImage> findBySessionIdWithDetails(@Param("sessionId") Integer sessionId);
}