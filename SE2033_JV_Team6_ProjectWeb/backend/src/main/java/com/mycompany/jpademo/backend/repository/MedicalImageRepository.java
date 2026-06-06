package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.MedicalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalImageRepository extends JpaRepository<MedicalImage, Integer> {
    List<MedicalImage> findByDiagnosisSession_SessionId(Integer sessionId);
}