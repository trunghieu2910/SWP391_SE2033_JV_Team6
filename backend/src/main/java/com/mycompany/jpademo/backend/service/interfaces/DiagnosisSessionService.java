package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;

public interface DiagnosisSessionService {
    /**
     * Bác sĩ tạo phiên khám mới
     */
    DiagnosisSessionResponse createSession(CreateDiagnosisSessionRequest request, Integer doctorId);

    /**
     * Thêm bệnh nhân vào phiên khám (click dấu cộng)
     */
    DiagnosisSessionResponse addPatientToSession(CreateDiagnosisSessionRequest request, Integer doctorId);

    /**
     * Bệnh nhân hoặc bác sĩ submit biểu mẫu triệu chứng
     */
    SymptomResultResponse submitSymptomForm(Integer sessionId, SubmitSymptomFormRequest request, Integer userId, String userRole);

    /**
     * Lấy chi tiết phiên khám
     */
    DiagnosisSessionResponse getSessionDetail(Integer sessionId);

    /**
     * Lấy chi tiết SymptomResult của phiên khám
     */
    SymptomResultResponse getSymptomResult(Integer sessionId);
}
