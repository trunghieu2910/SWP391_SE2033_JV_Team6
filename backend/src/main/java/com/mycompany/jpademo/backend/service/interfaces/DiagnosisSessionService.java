package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;

public interface DiagnosisSessionService {
    DiagnosisSessionResponse createSession(CreateDiagnosisSessionRequest request, Integer doctorId);

    DiagnosisSessionResponse addPatientToSession(CreateDiagnosisSessionRequest request, Integer doctorId);

    SymptomResultResponse submitSymptomForm(Integer sessionId, SubmitSymptomFormRequest request, Integer userId, String userRole);

    DiagnosisSessionResponse getSessionDetail(Integer sessionId);

    SymptomResultResponse getSymptomResult(Integer sessionId);
}
