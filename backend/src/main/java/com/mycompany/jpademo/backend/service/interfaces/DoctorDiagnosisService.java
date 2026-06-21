package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSymptomsRequest;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorDiagnosisService {
    Page<DoctorSessionResponse> getSessionsByDoctor(Integer doctorId, Pageable pageable, String keyword);

    void updateSessionStatus(Integer doctorId, UpdateSessionStatusRequest request);

    void updateSessionShare(Integer doctorId, UpdateSessionShareRequest request);

    List<SymptomResponse> getSessionSymptoms(Integer sessionId);

    void updateSessionSymptoms(Integer doctorId, UpdateSymptomsRequest request);
}
