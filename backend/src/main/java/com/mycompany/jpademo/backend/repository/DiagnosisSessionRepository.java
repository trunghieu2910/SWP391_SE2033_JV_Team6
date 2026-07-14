package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Integer> {

    // ===== QUERIES CHO MedicalRecord (dùng JOIN qua SymptomResult theo DB mới) =====
    @Query(value = """
        SELECT
            ds.sessionID AS id,
            COALESCE(u.fullName, '') AS patientName,
            r.finalDiagnosis AS diagnosis,
            ds.createdAt AS visitDate,
            s.symptomName AS symptoms,
            r.treatmentPlan AS prescription,
            r.doctorAdvice AS doctorNotes
        FROM DiagnosisSession ds
        LEFT JOIN Patient p ON ds.patientID = p.patientID
        LEFT JOIN Users u ON p.userID = u.userID
        LEFT JOIN Review r ON r.sessionID = ds.sessionID
        LEFT JOIN SymptomResult sr ON sr.sessionID = ds.sessionID
        LEFT JOIN SymptomDetails sd ON sd.symptomResultID = sr.symptomResultID
        LEFT JOIN Symptom s ON sd.symptomID = s.symptomID
        """, nativeQuery = true)
    List<Map<String, Object>> findAllMedicalRecords();

    @Query(value = """
        SELECT
            ds.sessionID AS id,
            ds.isShared AS isShared,
            COALESCE(u.fullName, '') AS patientName,
            r.finalDiagnosis AS diagnosis,
            ds.createdAt AS visitDate,
            ISNULL((SELECT TOP 1 sym.symptomName
                   FROM SymptomResult sr
                   JOIN SymptomDetails sd ON sd.symptomResultID = sr.symptomResultID
                   JOIN Symptom sym ON sd.symptomID = sym.symptomID
                   WHERE sr.sessionID = ds.sessionID), N'Không có triệu chứng') AS symptoms,
            r.treatmentPlan AS prescription,
            r.doctorAdvice AS doctorNotes
        FROM DiagnosisSession ds
        LEFT JOIN Patient p ON ds.patientID = p.patientID
        LEFT JOIN Users u ON p.userID = u.userID
        LEFT JOIN Review r ON r.sessionID = ds.sessionID
        WHERE p.patientID = :patientId
        ORDER BY ds.createdAt DESC
        """, nativeQuery = true)
    List<Map<String, Object>> findMedicalRecordsByPatientId(@Param("patientId") Integer patientId);

    // ===== QUERY CHO MedicalRecord với filter + pagination (từ folder 'sua') =====
    @Query(value = "SELECT " +
            "s.sessionID as id, " +
            "s.isShared as isShared, " +
            "s.status as status, " +
            "u.fullName as patientName, " +
            "  u.nationalID as nationalID, " +
            "  p.gender as gender, " +
            " du.fullName AS doctorFullName, " +
            "ISNULL(r.finalDiagnosis, N'Chưa có chẩn đoán') as diagnosis, " +
            "s.createdAt as visitDate, " +
            "ISNULL((SELECT TOP 1 sym.symptomName FROM SymptomResult sr " +
            "  JOIN SymptomDetails sd ON sd.symptomResultID = sr.symptomResultID " +
            "  JOIN Symptom sym ON sd.symptomID = sym.symptomID " +
            "  WHERE sr.sessionID = s.sessionID), N'Không có triệu chứng') as symptoms, " +
            "ISNULL(r.treatmentPlan, N'Chưa có đơn thuốc') as prescription, " +
            "ISNULL(r.doctorAdvice, N'Chưa có lời dặn') as doctorNotes " +
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [Users] u ON p.userID = u.userID " +
            "JOIN [Users] du ON s.userID = du.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID " +
            "WHERE (:keyword IS NULL OR u.fullName COLLATE Latin1_General_CI_AI\n" +
            "LIKE CONCAT('%', :keyword, '%') OR u.nationalID LIKE CONCAT('%', :keyword, '%')) " +
            "  AND (:status IS NULL OR s.status = :status) " +
            "  AND (:isShared IS NULL OR s.isShared = :isShared) " +
            "ORDER BY s.createdAt DESC",
            nativeQuery = true)
    List<Map<String, Object>> getMedicalRecords(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("isShared") Boolean isShared);

    @Query(value = """
    SELECT ds.* FROM DiagnosisSession ds
    LEFT JOIN Patient p ON ds.patientID = p.patientID
    LEFT JOIN [Users] u ON p.userID = u.userID
    WHERE ds.userID = :doctorId
      AND (:keyword IS NULL OR 
           u.fullName COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR 
           u.nationalID LIKE CONCAT('%', :keyword, '%'))
    """,
            countQuery = """
    SELECT COUNT(*) FROM DiagnosisSession ds
    LEFT JOIN Patient p ON ds.patientID = p.patientID
    LEFT JOIN [Users] u ON p.userID = u.userID
    WHERE ds.userID = :doctorId
      AND (:keyword IS NULL OR 
           u.fullName COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR 
           u.nationalID LIKE CONCAT('%', :keyword, '%'))
    """,
            nativeQuery = true)
    Page<DiagnosisSession> searchByDoctorWithKeyword(
            @Param("doctorId") Integer doctorId,
            @Param("keyword") String keyword,
            Pageable pageable);

    List<DiagnosisSession> findByPatientPatientId(Integer patientId);

    @Query(value = """
    SELECT 
        FORMAT(createdAt, 'yyyy-MM') as month,
        COUNT(*) as count
    FROM DiagnosisSession 
    WHERE createdAt >= DATEADD(month, -6, GETDATE())
    GROUP BY FORMAT(createdAt, 'yyyy-MM')
    ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getDiagnosisSessionsByMonth();

    @Query("SELECT ds FROM DiagnosisSession ds " +
            "LEFT JOIN FETCH ds.patient p " +
            "LEFT JOIN FETCH p.user pu " +
            "LEFT JOIN FETCH ds.user u " +
            "WHERE ds.sessionId = :sessionId")
    Optional<DiagnosisSession> findSessionWithDetails(@Param("sessionId") Integer sessionId);

    @Query("SELECT DISTINCT ds FROM DiagnosisSession ds " +
            "LEFT JOIN FETCH ds.symptomResult sr " +
            "LEFT JOIN FETCH sr.symptomDetails sd " +
            "LEFT JOIN FETCH sd.symptom " +
            "WHERE ds.patient.patientId = :patientId")
    List<DiagnosisSession> findByPatientPatientIdWithDetails(@Param("patientId") Integer patientId);

    @Query("SELECT ds FROM DiagnosisSession ds " +
            "WHERE ds.user.userId = :doctorId " +
            "AND (:keyword IS NULL OR LOWER(ds.patient.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR ds.status = :status) " +
            "AND (:startDate IS NULL OR ds.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR ds.createdAt <= :endDate)")
    Page<DiagnosisSession> searchByDoctorWithKeywordAndStatus(
            @Param("doctorId") Integer doctorId,
            @Param("keyword") String keyword,
            @Param("status") DiagnosisSessionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query(value = """
    SELECT 
        FORMAT(createdAt, 'yyyy-MM') as month,
        COUNT(*) as count
    FROM DiagnosisSession 
    WHERE (:startDate IS NULL OR createdAt >= :startDate)
      AND (:endDate IS NULL OR createdAt <= :endDate)
    GROUP BY FORMAT(createdAt, 'yyyy-MM')
    ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getDiagnosisSessionsByMonthWithFilter(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    @Query("SELECT COUNT(ds) FROM DiagnosisSession ds WHERE " +
            "(CAST(:startDate AS timestamp) IS NULL OR ds.createdAt >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR ds.createdAt <= :endDate)")
    long countSessionsWithDateFilter(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('FORMAT', d.createdAt, 'MM/yyyy') as month, COUNT(d) as count " +
            "FROM DiagnosisSession d " +
            "WHERE (:start IS NULL OR d.createdAt >= :start) " +
            "AND (:end IS NULL OR d.createdAt <= :end) " +
            "GROUP BY FUNCTION('FORMAT', d.createdAt, 'MM/yyyy') " +
            "ORDER BY month ASC")
    List<Object[]> getMonthlyDiagnosisSessions(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}

