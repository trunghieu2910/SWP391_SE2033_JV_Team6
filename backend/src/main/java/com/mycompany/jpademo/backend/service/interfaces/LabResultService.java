package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateLabResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;

import java.util.List;

/**
 * Business operations for creating, viewing, and deleting lab test
 * orders on a diagnosis session. Authorization (ownership checks) is
 * enforced inside the implementation, not by the caller.
 */
public interface LabResultService {

    /**
     * Orders a new lab test for a session. Only the doctor who owns
     * the session may call this; the test type must be one of the
     * types supported by {@code LisMockDataProvider}, and the session
     * must not already be COMPLETED or already have an order of the
     * same test type.
     */
    LabResultResponse createLabResult(CreateLabResultRequest request);

    /**
     * Lists all lab results (with parameters, if completed) for a
     * session. Visible to: the owning patient (if the session has
     * been shared with them) and any doctor; blocked for all other
     * roles.
     */
    List<LabResultResponse> getLabResultsBySession(Integer sessionId);

    /**
     * Deletes a lab order. Only the doctor who owns the session may
     * delete it, and only while it is still PENDING — a COMPLETED
     * order (already has recorded results) cannot be removed.
     */
    void deleteLabResult(Integer labResultId);

}
