package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateLabResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;

import java.util.List;

public interface LabResultService {

    LabResultResponse createLabResult (CreateLabResultRequest request);

    List<LabResultResponse> getLabResultsBySession (Integer sessionId);
}
