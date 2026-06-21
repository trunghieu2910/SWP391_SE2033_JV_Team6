package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Integer> {
    List<LabResult> findByDiagnosisSessionSessionId(Integer sessionId);

    /**
     * Tìm LabResult theo labResultId và kiểm tra trạng thái bằng enum
     * (dùng để đảm bảo chỉ xử lý các bản ghi đang PENDING từ LIS).
     */
    Optional<LabResult> findByLabResultIdAndStatus(Integer labResultId, LabResultStatus status);
}