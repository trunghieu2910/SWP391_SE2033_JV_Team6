package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.PatientSearchResponse;

import java.util.List;

public interface PatientSearchService {
    /**
     * Tìm kiếm bệnh nhân theo từ khóa (fullName hoặc nationalId)
     */
    List<PatientSearchResponse> searchPatients(String keyword);
}
