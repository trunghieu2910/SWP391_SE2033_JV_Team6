package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalRecordService {
    Page<MedicalRecordResponse> getMedicalRecords(
            String keyword,
            String status,
            Boolean isShared,
            String diseaseType,
            LocalDateTime startDate,   
            LocalDateTime endDate,
            Pageable pageable);



    // 3. Hàm bóc tách chi tiết toàn bộ dữ liệu sạch của 1 ca khám cụ thể
    MedicalRecordDetailResponse getMedicalRecordDetail(Integer sessionID, boolean isPatient);



}