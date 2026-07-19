package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.UpdateClinicalSymptomsRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionDetailResponse;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResponse;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DoctorDiagnosisService {

    Page<DoctorSessionResponse> getSessionsByDoctor(Integer doctorId, Pageable pageable,
                                                    String keyword, DiagnosisSessionStatus status,
                                                    LocalDate startDate, LocalDate endDate);

    void updateSessionStatus(Integer doctorId, UpdateSessionStatusRequest request);

    void updateSessionShare(Integer doctorId, UpdateSessionShareRequest request);

    List<SymptomResponse> getSessionSymptoms(Integer sessionId, Integer doctorId);

    void updateClinicalSymptoms(Integer doctorId, Integer sessionId, UpdateClinicalSymptomsRequest request);

    void setClinicalInputMode(Integer doctorId, Integer sessionId, com.mycompany.jpademo.backend.enums.ClinicalInputMode clinicalInputMode);

    DoctorSessionDetailResponse getSessionDetail(Integer sessionId, Integer doctorId);

    void saveReview(Integer doctorId, Integer sessionId, com.mycompany.jpademo.backend.dto.request.CreateReviewRequest request);
}
