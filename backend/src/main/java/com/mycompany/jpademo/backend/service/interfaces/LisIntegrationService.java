package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;

public interface LisIntegrationService {

    /**
     * Nhận dữ liệu kết quả xét nghiệm mô phỏng từ LIS, ghi vào LabResult
     * chỉ định bởi labResultId (phải đang PENDING) và đánh dấu COMPLETED.
     */
    LabResultResponse receiveLabResults(LisResultRequest request, String source);
}

