package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreatePatientSessionRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.entity.MedicalImage;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.SymptomResult;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.enums.MedicalImageStatus;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.LabResultRepository;
import com.mycompany.jpademo.backend.repository.MedicalImageRepository;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.repository.SymptomResultRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;

@RestController
@RequestMapping("/api/patient/sessions")
@RequiredArgsConstructor
public class PatientDiagnosisController {
    private final PatientRepository patientRepository;
    private final DiagnosisSessionRepository sessionRepository;
    private final SymptomResultRepository symptomResultRepository;
    private final LabResultRepository labResultRepository;
    private final MedicalImageRepository medicalImageRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<DiagnosisSessionResponse>> createSessionForPatient(
            @RequestBody(required = false) CreatePatientSessionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        var user = userDetails.getUser();
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy patient cho user"));

        DiagnosisSession session = DiagnosisSession.builder()
                .patient(patient)
                .user(patient.getUser())
                .weight(request != null ? request.getWeight() : null)
                .height(request != null ? request.getHeight() : null)
                .status(DiagnosisSessionStatus.PENDING)
                .build();

        DiagnosisSession saved = sessionRepository.save(session);

        SymptomResult symptomResult = SymptomResult.builder()
                .diagnosisSession(saved)
                .status(SymptomResultStatus.PENDING)
                .build();
        symptomResultRepository.save(symptomResult);

        LabResult labResult = LabResult.builder()
                .diagnosisSession(saved)
                .testType("Xét nghiệm máu tổng quát")
                .status(LabResultStatus.PENDING)
                .build();
        labResultRepository.save(labResult);

        MedicalImage medicalImage = MedicalImage.builder()
                .diagnosisSession(saved)
                .imageType("Siêu âm")
                .status(MedicalImageStatus.PENDING)
                .build();
        medicalImageRepository.save(medicalImage);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DiagnosisSessionResponse>builder()
                        .code(201)
                        .success(true)
                        .message("Tạo phiên khám thành công")
                        .data(DiagnosisSessionResponse.builder()
                                .sessionId(saved.getSessionId())
                                .patientId(patient.getPatientId())
                                .patientName(patient.getUser().getFullName())
                                .status(saved.getStatus())
                                .symptomResultStatus(symptomResult.getStatus())
                                .createdAt(saved.getCreatedAt())
                                .build())
                        .build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<DiagnosisSessionResponse>> getActiveSession(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy patient cho user"));

        var sessions = sessionRepository.findByPatientPatientId(patient.getPatientId());
        var active = sessions.stream()
                .filter(s -> s.getStatus() != DiagnosisSessionStatus.COMPLETED)
                .max(Comparator.comparing(DiagnosisSession::getCreatedAt))
                .orElse(null);

        DiagnosisSessionResponse resp = null;
        if (active != null) {
            resp = DiagnosisSessionResponse.builder()
                    .sessionId(active.getSessionId())
                    .patientId(active.getPatient().getPatientId())
                    .patientName(active.getPatient().getUser().getFullName())
                    .status(active.getStatus())
                    .symptomResultStatus(active.getSymptomResult() != null ? active.getSymptomResult().getStatus() : null)
                    .createdAt(active.getCreatedAt())
                    .build();
        }

        return ResponseEntity.ok(ApiResponse.<DiagnosisSessionResponse>builder()
                .code(200)
                .success(true)
                .message("Lấy phiên khám đang hoạt động")
                .data(resp)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<DiagnosisSessionResponse>>> getSessions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy patient cho user"));

        var sessions = sessionRepository.findByPatientPatientId(patient.getPatientId());

        var list = sessions.stream().map(s -> DiagnosisSessionResponse.builder()
                .sessionId(s.getSessionId())
                .patientId(s.getPatient().getPatientId())
                .patientName(s.getPatient().getUser().getFullName())
                .status(s.getStatus())
                .symptomResultStatus(s.getSymptomResult() != null ? s.getSymptomResult().getStatus() : null)
                .createdAt(s.getCreatedAt())
                .build()).toList();

        return ResponseEntity.ok(ApiResponse.<java.util.List<DiagnosisSessionResponse>>builder()
                .code(200)
                .success(true)
                .message("Lấy danh sách phiên khám")
                .data(list)
                .build());
    }
}
