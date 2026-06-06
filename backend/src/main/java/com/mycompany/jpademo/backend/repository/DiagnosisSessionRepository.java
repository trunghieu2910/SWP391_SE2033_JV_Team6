package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Integer> {

    @Query(value = """
        SELECT
            ds.sessionID AS id,
            COALESCE(u.fullName, '') AS patientName,
            r.finalDiagnosis AS diagnosis,
            CONVERT(VARCHAR, ds.createdAt, 120) AS visitDate,
            s.symptomName AS symptoms,
            r.treatmentPlan AS prescription,
            r.doctorAdvice AS doctorNotes
        FROM DiagnosisSession ds
        LEFT JOIN Patient p ON ds.patientID = p.patientID
        LEFT JOIN Users u ON p.userID = u.userID
        LEFT JOIN Review r ON r.sessionID = ds.sessionID
        LEFT JOIN SymptomDetails sd ON sd.sessionID = ds.sessionID
        LEFT JOIN Symptom s ON sd.symptomID = s.symptomID
        """, nativeQuery = true)
    List<Map<String, Object>> findAllMedicalRecords();

    @Query(value = """
        SELECT
            ds.sessionID AS id,
            COALESCE(u.fullName, '') AS patientName,
            r.finalDiagnosis AS diagnosis,
            CONVERT(VARCHAR, ds.createdAt, 120) AS visitDate,
            s.symptomName AS symptoms,
            r.treatmentPlan AS prescription,
            r.doctorAdvice AS doctorNotes
        FROM DiagnosisSession ds
        LEFT JOIN Patient p ON ds.patientID = p.patientID
        LEFT JOIN Users u ON p.userID = u.userID
        LEFT JOIN Review r ON r.sessionID = ds.sessionID
        LEFT JOIN SymptomDetails sd ON sd.sessionID = ds.sessionID
        LEFT JOIN Symptom s ON sd.symptomID = s.symptomID
        WHERE p.patientID = :patientId
        """, nativeQuery = true)
    List<Map<String, Object>> findMedicalRecordsByPatientId(@Param("patientId") Integer patientId);
}
