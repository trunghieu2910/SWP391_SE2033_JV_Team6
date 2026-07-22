package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.DoctorWorkloadResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.dto.request.CreatePatientSessionRequest;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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

    // [Nguyen The Hieu]: Bước 2 - Service: Khai báo hàm lấy danh sách tải của TẤT CẢ bác sĩ đang hoạt động (cho Màn hình 1)
    List<DoctorWorkloadResponse> getDoctorWorkloads();

    // [Nguyen The Hieu]: Bước 2 - Service: Khai báo hàm lấy chi tiết các ca khám của MỘT bác sĩ cụ thể, có hỗ trợ lọc ngày (cho Màn hình 2)
    Page<DiagnosisSession> getSessionsByDoctor(Integer doctorId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}

