package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Integer> {

    @Query(value = "SELECT " +
            "s.sessionID as id, " +
            "s.isShared as isShared, " +
            "u.fullName as patientName, " +
            "ISNULL(r.finalDiagnosis, N'Chưa có chẩn đoán') as diagnosis, " +
            "CONVERT(VARCHAR, s.createdAt, 105) as visitDate, " +
            "ISNULL((SELECT TOP 1 sym.symptomName FROM SymptomResult sr " +
            "  JOIN SymptomDetails sd ON sd.symptomResultID = sr.symptomResultID " +
            "  JOIN Symptom sym ON sd.symptomID = sym.symptomID " +
            "  WHERE sr.sessionID = s.sessionID), N'Không có triệu chứng') as symptoms, " +
            "ISNULL(r.treatmentPlan, N'Chưa có đơn thuốc') as prescription, " +
            "ISNULL(r.doctorAdvice, N'Chưa có lời dặn') as doctorNotes " +
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [Users] u ON p.userID = u.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID " +
            "WHERE (:keyword IS NULL OR u.fullName LIKE CONCAT('%', :keyword, '%') OR u.nationalID LIKE CONCAT('%', :keyword, '%')) " +
            "  AND (:status IS NULL OR s.status = :status) " +
            "  AND (:isShared IS NULL OR s.isShared = :isShared) " +
            "ORDER BY s.createdAt DESC",
            nativeQuery = true)
    List<Map<String, Object>> getMedicalRecords(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("isShared") Boolean isShared);

    List<DiagnosisSession> findByPatientPatientId(Integer patientId);
}