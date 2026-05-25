package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Integer> {
    // Hàm lấy danh sách tất cả các ca khám cũ của một bệnh nhân dựa vào ID của họ
    List<DiagnosisSession> findByPatientPatientID(Integer patientID);
}