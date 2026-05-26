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
    // bac si
    @Query(value = "SELECT " +
            "s.sessionID as id, " +
            "u.fullName as patientName, " +
            "r.finalDiagnosis as diagnosis, " +
            "CONVERT(VARCHAR, s.createdAt, 105) as visitDate, " +
            "ISNULL((SELECT TOP 1 sym.symptomName FROM SymptomDetails sd JOIN Symptom sym ON sd.symptomID = sym.symptomID WHERE sd.sessionID = s.sessionID), N'Không có triệu chứng') as symptoms, " +
            "ISNULL(r.treatmentPlan, N'Chưa có đơn thuốc') as prescription, " +
            "ISNULL(r.doctorAdvice, N'Chưa có lời dặn') as doctorNotes " +
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [User] u ON p.userID = u.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID", nativeQuery = true)
    List<Map<String, Object>> findAllMedicalRecords();

    // benh nhan
    @Query(value = "SELECT " +
            "s.sessionID as id, " +
            "u.fullName as patientName, " +
            "r.finalDiagnosis as diagnosis, " +
            "CONVERT(VARCHAR, s.createdAt, 105) as visitDate, " +
            "ISNULL((SELECT TOP 1 sym.symptomName FROM SymptomDetails sd JOIN Symptom sym ON sd.symptomID = sym.symptomID WHERE sd.sessionID = s.sessionID), N'Không có triệu chứng') as symptoms, " +
            "ISNULL(r.treatmentPlan, N'Chưa có đơn thuốc') as prescription, " +
            "ISNULL(r.doctorAdvice, N'Chưa có lời dặn') as doctorNotes " +
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [User] u ON p.userID = u.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID " +
            "WHERE p.patientID = :patientId", nativeQuery = true)
    List<Map<String, Object>> findMedicalRecordsByPatientId(@Param("patientId") Integer patientId);



    @Query(value = "SELECT * FROM DiagnosisSession WHERE patientID = :patientId", nativeQuery = true)
    List<DiagnosisSession> findByPatientPatientId(@Param("patientId") Integer patientId);
}