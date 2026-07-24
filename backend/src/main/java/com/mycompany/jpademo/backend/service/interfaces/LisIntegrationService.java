package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;

/**
 * Applies simulated lab result values (from LisMockDataProvider) to a
 * pending LabResult on behalf of the doctor who triggered the
 * "Lấy kết quả giả lập" action. This project no longer integrates
 * with a real external LIS/HIS system.
 */
public interface LisIntegrationService {

    /**
     * Applies a set of result values to the LabResult identified by
     * {@code request.getLabResultId()} (which must currently be
     * PENDING) and marks it COMPLETED.
     *
     */
    LabResultResponse receiveLabResults(LisResultRequest request);
}

