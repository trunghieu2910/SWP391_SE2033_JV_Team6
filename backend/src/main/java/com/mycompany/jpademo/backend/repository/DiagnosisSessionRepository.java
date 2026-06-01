package com.mycompany.jpademo.backend.repository;

import org.springframework.data.repository.query.Param;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Integer> {

    // 1. DÀNH CHO BÁC SĨ 
    @Query(value = "SELECT " +
            "s.sessionID as id, " +
            "u.fullName as patientName, " +
            "r.finalDiagnosis as diagnosis, " +
            "CONVERT(VARCHAR, s.createdAt, 105) as visitDate, " +
            "ISNULL((SELECT TOP 1 sym.symptomName FROM SymptomDetails sd JOIN Symptom sym ON sd.symptomID = sym.symptomID WHERE sd.sessionID = s.sessionID), N'Không có triệu chứng') as symptoms, " +
            "ISNULL(r.treatmentPlan, N'Chưa có đơn thuốc') as prescription, " +
            "ISNULL(r.doctorAdvice, N'Chưa có lời dặn') as doctorNotes, " +
            "ISNULL(s.isShared, 0) as isShared " + // Lấy trạng thái khóa/mở
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [Users] u ON p.userID = u.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID", nativeQuery = true)
    List<Map<String, Object>> findAllMedicalRecords();

    // 2. DÀNH CHO BỆNH NHÂN
    @Query(value = "SELECT " +
            "s.sessionID as id, " +
            "u.fullName as patientName, " +
            "CASE WHEN s.isShared = 1 THEN r.finalDiagnosis ELSE N'Đang chờ bác sĩ công bố...' END as diagnosis, " +
            "CONVERT(VARCHAR, s.createdAt, 105) as visitDate, " +
            "ISNULL((SELECT TOP 1 sym.symptomName FROM SymptomDetails sd JOIN Symptom sym ON sd.symptomID = sym.symptomID WHERE sd.sessionID = s.sessionID), N'Không có triệu chứng') as symptoms, " +
            "CASE WHEN s.isShared = 1 THEN ISNULL(r.treatmentPlan, N'Chưa có đơn thuốc') ELSE N'Bảo mật' END as prescription, " +
            "CASE WHEN s.isShared = 1 THEN ISNULL(r.doctorAdvice, N'Chưa có lời dặn') ELSE N'Bảo mật' END as doctorNotes " +
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [Users] u ON p.userID = u.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID " +
            "WHERE p.patientID = :patientId", nativeQuery = true)
    List<Map<String, Object>> findMedicalRecordsByPatientId(@Param("patientId") Integer patientId);

    @Query(value = "SELECT * FROM DiagnosisSession WHERE patientID = :patientId", nativeQuery = true)
    List<DiagnosisSession> findByPatientPatientId(@Param("patientId") Integer patientId);
}