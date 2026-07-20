package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.dto.request.CreatePatientSessionRequest;
import com.mycompany.jpademo.backend.entity.User;
import java.util.List;

public interface DiagnosisSessionService {
    DiagnosisSessionResponse createSession(CreateDiagnosisSessionRequest request, Integer creatorId);

    DiagnosisSessionResponse addPatientToSession(CreateDiagnosisSessionRequest request, Integer creatorId);

    SymptomResultResponse submitSymptomForm(Integer sessionId, SubmitSymptomFormRequest request, Integer userId, String userRole);

    DiagnosisSessionResponse getSessionDetail(Integer sessionId);

    SymptomResultResponse getSymptomResult(Integer sessionId);

    DiagnosisSessionResponse createSessionForPatient(CreatePatientSessionRequest request, User user);

    DiagnosisSessionResponse getActiveSessionForPatient(User user);

    List<DiagnosisSessionResponse> getSessionsForPatient(User user);

    List<DiagnosisSessionResponse> getPendingUltrasoundSessions();
    List<DiagnosisSessionResponse> getCompletedUltrasoundSessions();
}
