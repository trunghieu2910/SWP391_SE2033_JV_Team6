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

    /**
     * [Role: Lễ tân]
     * Chức năng: Đếm số lượng ca khám của một bác sĩ đang ở trạng thái khác với trạng thái truyền vào (ví dụ: đếm các ca chưa COMPLETED).
     * Mục đích: Lễ tân dùng để xem số lượng ca bệnh đang chờ xử lý của bác sĩ để phân bổ bệnh nhân cho hợp lý.
     */
    long countByUserUserIdAndStatusNot(Integer userId, DiagnosisSessionStatus status);

    long countByUserUserIdAndStatusNotIn(Integer userId, List<DiagnosisSessionStatus> statuses);

    /**
     * [Role: Bệnh nhân]
     * Chức năng: Lấy danh sách toàn bộ hồ sơ bệnh án của một bệnh nhân cụ thể.
     * Mục đích: Hiển thị lịch sử khám bệnh cho Bệnh nhân trong màn hình "Hồ sơ bệnh án" của họ.
     */
    @Query(value = """
        SELECT
            ds.sessionID AS id,
            ds.isShared AS isShared,
            ds.status AS status,
            COALESCE(u.fullName, '') AS patientName,
            dt.name AS diagnosis,
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
        LEFT JOIN DiseaseType dt ON r.diseaseTypeID = dt.diseaseTypeID
        WHERE p.patientID = :patientId
        ORDER BY ds.createdAt DESC
        """, nativeQuery = true)
    List<Map<String, Object>> findMedicalRecordsByPatientId(@Param("patientId") Integer patientId);

    // ===== QUERY CHO MedicalRecord với filter + pagination  =====
    /**
     * [Role: Bác sĩ / Admin]
     * Chức năng: Tìm kiếm và lọc danh sách hồ sơ bệnh án (bao gồm phân trang và nhiều tiêu chí lọc như tên, CCCD, trạng thái, khoảng thời gian).
     * Mục đích: Hiển thị danh sách hồ sơ bệnh án tổng hợp trên giao diện quản lý của Bác sĩ hoặc Admin.
     */
    @Query(value = "SELECT " +
            "s.sessionID as id, " +
            "s.isShared as isShared, " +
            "s.status as status, " +
            "u.fullName as patientName, " +
            "  u.nationalID as nationalID, " +
            "  p.gender as gender, " +
            " du.fullName AS doctorFullName, " +
            "ISNULL(dt.name, N'Chưa có chẩn đoán') as diagnosis, " +
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
            "LEFT JOIN DiseaseType dt ON r.diseaseTypeID = dt.diseaseTypeID " +
            "WHERE (:keyword IS NULL OR u.fullName COLLATE Latin1_General_CI_AI " +
            "LIKE CONCAT('%', :keyword, '%') OR u.nationalID LIKE CONCAT('%', :keyword, '%') OR CAST(s.sessionID AS VARCHAR(25)) LIKE CONCAT('%', :keyword, '%')) " +
            "  AND (:status IS NULL OR s.status = :status) " +
            "  AND (:isShared IS NULL OR s.isShared = :isShared) " +
            "  AND (:diseaseType IS NULL OR dt.name = :diseaseType) " +
            "  AND (:startDate IS NULL OR r.reviewedAt >= :startDate) " +
            "  AND (:endDate IS NULL OR r.reviewedAt <= :endDate) " +
            "ORDER BY s.createdAt DESC",
            nativeQuery = true)
    List<Map<String, Object>> getMedicalRecords(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("isShared") Boolean isShared,
            @Param("diseaseType") String diseaseType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);       


    /**
     * [Role: Hệ thống / Backend]
     * Chức năng: Tìm tất cả các ca khám cơ bản của một bệnh nhân (truy vấn Entity cơ bản).
     */
    List<DiagnosisSession> findByPatientPatientId(Integer patientId);



    /**
     * [Role: Bác sĩ / Kỹ thuật viên / Lễ tân]
     * Chức năng: Lấy chi tiết một ca khám kèm theo thông tin Bệnh nhân, User (bệnh nhân) và Bác sĩ phụ trách.
     * Mục đích: Dùng FETCH JOIN để tối ưu hiệu suất (tránh lỗi N+1 query) khi cần lấy thông tin tổng quan của ca khám.
     */
    @Query("SELECT ds FROM DiagnosisSession ds " +
            "LEFT JOIN FETCH ds.patient p " +
            "LEFT JOIN FETCH p.user pu " +
            "LEFT JOIN FETCH ds.user u " +
            "WHERE ds.sessionId = :sessionId")
    Optional<DiagnosisSession> findSessionWithDetails(@Param("sessionId") Integer sessionId);

    /**
     * [Role: Bác sĩ]
     * Chức năng: Lấy danh sách ca khám của bệnh nhân kèm theo thông tin chi tiết về kết quả triệu chứng ban đầu.
     * Mục đích: Tránh N+1 query, dùng để xem nhanh các triệu chứng cũ của bệnh nhân trong các lần khám trước.
     */
    @Query("SELECT DISTINCT ds FROM DiagnosisSession ds " +
            "LEFT JOIN FETCH ds.symptomResult sr " +
            "LEFT JOIN FETCH sr.symptomDetails sd " +
            "LEFT JOIN FETCH sd.symptom " +
            "WHERE ds.patient.patientId = :patientId")
    List<DiagnosisSession> findByPatientPatientIdWithDetails(@Param("patientId") Integer patientId);

    /**
     * [Role: Bác sĩ]
     * Chức năng: Lấy danh sách ca chẩn đoán được giao cho một bác sĩ cụ thể, hỗ trợ lọc theo trạng thái và thời gian.
     * Mục đích: Hiển thị danh sách các ca bệnh đang chờ xử lý, đang xử lý hoặc đã hoàn thành trên màn hình làm việc chính của Bác sĩ.
     */
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




    /**
     * [Role: Admin]
     * Chức năng: Đếm tổng số lượng ca khám trong một khoảng thời gian cụ thể.
     * Mục đích: Hiển thị số liệu thống kê tổng quan (ví dụ: Tổng số ca khám tháng này) trên Dashboard của Admin.
     */
    @Query("SELECT COUNT(ds) FROM DiagnosisSession ds WHERE " +
            "(CAST(:startDate AS timestamp) IS NULL OR ds.createdAt >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR ds.createdAt <= :endDate)")
    long countSessionsWithDateFilter(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);




    
    /**
     * [Role: Kỹ thuật viên (Xét nghiệm / Siêu âm)]
     * Chức năng: Lấy danh sách ca khám đang ở trong một danh sách các trạng thái nhất định (ví dụ PENDING, PROCESSING).
     * Mục đích: Lấy ra các ca bệnh đang chờ hoặc đang được thực hiện các chỉ định cận lâm sàng.
     */
    List<DiagnosisSession> findByStatusInOrderByCreatedAtDesc(List<DiagnosisSessionStatus> statuses);

    // [Nguyen The Hieu]: Bước 1 - Repository: Đếm số ca khám của một bác sĩ dựa theo một trạng thái (status) cụ thể.
    long countByUserUserIdAndStatus(Integer userId, DiagnosisSessionStatus status);

    // [Nguyen The Hieu]: Bước 1 - Repository: Lấy danh sách ca khám của một bác sĩ.
    // Hỗ trợ bộ lọc theo khoảng thời gian (startDate, endDate) và phân trang (Pageable) cho màn hình chi tiết.
    @Query("SELECT ds FROM DiagnosisSession ds " +
            "LEFT JOIN FETCH ds.patient p " +
            "LEFT JOIN FETCH p.user pu " +
            "WHERE ds.user.userId = :doctorId " +
            "AND (:statuses IS NULL OR ds.status IN :statuses) " +
            "AND (:startDate IS NULL OR ds.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR ds.createdAt <= :endDate) " +
            "ORDER BY ds.createdAt DESC")
    Page<DiagnosisSession> findByDoctorIdWithDateFilter(
            @Param("doctorId") Integer doctorId,
            @Param("statuses") List<DiagnosisSessionStatus> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ===== QUERIES ĐẾM SỐ LƯỢNG CHO TRANG THỐNG KÊ (CHÍNH XÁC - KHÔNG PHỤ THUỘC PHÂN TRANG) =====

    /**
     * [Role: Bác sĩ / Admin]
     * Chức năng: Đếm tổng số lượng hồ sơ bệnh án phù hợp với bộ lọc tìm kiếm (không phụ thuộc vào số lượng phân trang).
     * Mục đích: Trả về con số chính xác để làm tổng số bản ghi (Total Elements) hỗ trợ việc phân trang trên giao diện.
     */
    @Query(value = "SELECT COUNT(s.sessionID) " +
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [Users] u ON p.userID = u.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID " +
            "LEFT JOIN DiseaseType dt ON r.diseaseTypeID = dt.diseaseTypeID " +
            "WHERE (:keyword IS NULL OR u.fullName COLLATE Latin1_General_CI_AI " +
            "LIKE CONCAT('%', :keyword, '%') OR u.nationalID LIKE CONCAT('%', :keyword, '%')) " +
            "  AND (:status IS NULL OR s.status = :status) " +
            "  AND (:diseaseType IS NULL OR dt.name = :diseaseType) " +
            "  AND (:startDate IS NULL OR r.reviewedAt >= :startDate) " +
            "  AND (:endDate IS NULL OR r.reviewedAt <= :endDate)",
            nativeQuery = true)
    long countMedicalRecordsByStatus(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("diseaseType") String diseaseType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * [Role: Admin]
     * Chức năng: Đếm tổng số lượng hồ sơ bệnh án đã có kết luận chẩn đoán bệnh (diseaseType IS NOT NULL).
     * Mục đích: Phục vụ cho tính năng báo cáo thống kê tỷ lệ các ca khám đã hoàn tất việc chẩn đoán thành công.
     */
    @Query(value = "SELECT COUNT(s.sessionID) " +
            "FROM DiagnosisSession s " +
            "JOIN Patient p ON s.patientID = p.patientID " +
            "JOIN [Users] u ON p.userID = u.userID " +
            "LEFT JOIN Review r ON s.sessionID = r.sessionID " +
            "LEFT JOIN DiseaseType dt ON r.diseaseTypeID = dt.diseaseTypeID " +
            "WHERE (:keyword IS NULL OR u.fullName COLLATE Latin1_General_CI_AI " +
            "LIKE CONCAT('%', :keyword, '%') OR u.nationalID LIKE CONCAT('%', :keyword, '%')) " +
            "  AND (dt.name IS NOT NULL) " +
            "  AND (:diseaseType IS NULL OR dt.name = :diseaseType) " +
            "  AND (:startDate IS NULL OR r.reviewedAt >= :startDate) " +
            "  AND (:endDate IS NULL OR r.reviewedAt <= :endDate)",
            nativeQuery = true)
    long countMedicalRecordsWithDiagnosis(
            @Param("keyword") String keyword,
            @Param("diseaseType") String diseaseType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

