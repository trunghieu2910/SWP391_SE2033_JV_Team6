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

/**
 * Author: GiangLTHE194888
 * Task: Service interface defining doctor diagnosis session actions, clinical symptoms, and medical imaging requests.
 */
public interface DoctorDiagnosisService {

    /** Retrieves doctor diagnosis sessions filtered by keyword, status, and dates. */
    Page<DoctorSessionResponse> getSessionsByDoctor(Integer doctorId, Pageable pageable,
                                                    String keyword, DiagnosisSessionStatus status,
                                                    LocalDate startDate, LocalDate endDate);

    /** Updates the status of a specific diagnosis session. */
    void updateSessionStatus(Integer doctorId, UpdateSessionStatusRequest request);

    /** Updates the sharing configuration of a diagnosis session. */
    void updateSessionShare(Integer doctorId, UpdateSessionShareRequest request);

    /** Retrieves all symptoms recorded for a specific diagnosis session. */
    List<SymptomResponse> getSessionSymptoms(Integer sessionId, Integer doctorId);

    /** Saves or updates clinical symptoms associated with a diagnosis session. */
    void updateClinicalSymptoms(Integer doctorId, Integer sessionId, UpdateClinicalSymptomsRequest request);

    /** Configures clinical input mode for symptom selection. */
    void setClinicalInputMode(Integer doctorId, Integer sessionId, com.mycompany.jpademo.backend.enums.ClinicalInputMode clinicalInputMode);

    /** Retrieves detailed session overview, patient data, and logs for a doctor. */
    DoctorSessionDetailResponse getSessionDetail(Integer sessionId, Integer doctorId);

    /** Saves the final review diagnosis and conclusions for a session. */
    void saveReview(Integer doctorId, Integer sessionId, com.mycompany.jpademo.backend.dto.request.CreateReviewRequest request);

    /** Deletes an imaging order from a diagnosis session. */
    void deleteMedicalImage(Integer doctorId, Integer sessionId, Integer imageId);

    /** Generates a new medical imaging order for a session. */
    void createMedicalImage(Integer doctorId, Integer sessionId, String imageType);

    void verifyDoctorOwnsSession(Integer doctorId, Integer sessionId);
}
