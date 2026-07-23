package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;

/**
 * Entry point shared by both the real LIS webhook and the
 * UI "simulate result" action for applying incoming lab result
 * values to a pending LabResult.
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

