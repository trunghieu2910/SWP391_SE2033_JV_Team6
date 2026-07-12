package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomDetailResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.enums.MedicalImageStatus;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosisSessionServiceImpl implements DiagnosisSessionService {
    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final SymptomRepository symptomRepository;
    private final SymptomResultRepository symptomResultRepository;
    private final SymptomDetailsRepository symptomDetailsRepository;
    private final LabResultRepository labResultRepository;
    private final MedicalImageRepository medicalImageRepository;
    private final SystemLogServiceImpl systemLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DiagnosisSessionResponse createSession(CreateDiagnosisSessionRequest request, Integer doctorId) {
        // Kiểm tra bệnh nhân tồn tại
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Bệnh nhân không tồn tại"));

        // Kiểm tra bác sĩ tồn tại
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));

        // Tạo DiagnosisSession
        DiagnosisSession session = DiagnosisSession.builder()
                .patient(patient)
                .user(doctor)
                .weight(request.getWeight())
                .height(request.getHeight())
                .status(DiagnosisSessionStatus.PENDING)
                .clinicalInputMode(ClinicalInputMode.PATIENT)
                .build();

        DiagnosisSession savedSession = diagnosisSessionRepository.save(session);

        // NOTE: do NOT create SymptomResult here. It will be created when patient or doctor submits.

        // Tạo LabResult
        LabResult labResult = LabResult.builder()
                .diagnosisSession(savedSession)
                .testType("Xét nghiệm máu tổng quát")
                .status(LabResultStatus.PENDING)
                .build();
        labResultRepository.save(labResult);

        // Tạo MedicalImage
        MedicalImage medicalImage = MedicalImage.builder()
                .diagnosisSession(savedSession)
                .imageType("Siêu âm")
                .status(MedicalImageStatus.PENDING)
                .build();
        medicalImageRepository.save(medicalImage);

        // Ghi log
        systemLogService.logActivity("DiagnosisSession", savedSession.getSessionId(), "CREATE",
                "Bác sĩ tạo phiên khám mới cho bệnh nhân " + patient.getUser().getFullName());

        // Emit event để notify patient
        eventPublisher.publishEvent(new DiagnosisSessionCreatedEvent(savedSession.getSessionId(), patient.getUser().getUserId()));

        return mapToResponse(savedSession);
    }

    @Override
    @Transactional
    public DiagnosisSessionResponse addPatientToSession(CreateDiagnosisSessionRequest request, Integer doctorId) {
        // Tương tự như createSession
        return createSession(request, doctorId);
    }

    @Override
    @Transactional
    public SymptomResultResponse submitSymptomForm(Integer sessionId, SubmitSymptomFormRequest request, Integer userId, String userRole) {
        // Kiểm tra phiên khám tồn tại
        DiagnosisSession session = diagnosisSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiên khám không tồn tại"));

        // Kiểm tra quyền: nếu là bệnh nhân, phải là bệnh nhân của phiên này
        if ("ROLE_PATIENT".equals(userRole)) {
            if (!session.getPatient().getUser().getUserId().equals(userId)) {
                throw new BadRequestException("Bạn không có quyền submit form cho phiên khám này");
            }
            if (session.getClinicalInputMode() == ClinicalInputMode.DOCTOR) {
                throw new BadRequestException("Phiên khám này được bác sĩ nhập triệu chứng. Bạn không thể gửi biểu mẫu.");
            }
        }

        // Lấy hoặc tạo SymptomResult nếu chưa tồn tại
        SymptomResult symptomResult = symptomResultRepository.findByDiagnosisSessionSessionId(sessionId)
            .orElseGet(() -> {
                SymptomResult sr = new SymptomResult();
                sr.setDiagnosisSession(session);
                sr.setStatus(SymptomResultStatus.PENDING);
                sr.setSymptomDetails(new java.util.ArrayList<>());
                return sr;
            });

        // Xóa các SymptomDetails cũ
        if ("ROLE_PATIENT".equals(userRole) && symptomResult.getStatus() == SymptomResultStatus.COMPLETED) {
            throw new BadRequestException("Bạn đã gửi triệu chứng rồi, không thể chỉnh sửa lại.");
        }
        if (symptomResult.getSymptomDetails() != null && !symptomResult.getSymptomDetails().isEmpty()) {
            symptomDetailsRepository.deleteAll(symptomResult.getSymptomDetails());
        }

        // Set thêm thông tin form triệu chứng
        symptomResult.setMenopauseStatus(request.getMenopauseStatus());
        symptomResult.setSymptomDuration(request.getSymptomDuration());
        symptomResult.setSymptomProgressing(request.getSymptomProgressing());

        // Save SymptomResult trước khi tạo SymptomDetails (để tránh transient entity error)
        symptomResultRepository.save(symptomResult);

        // Thêm SymptomDetails mới
        List<Symptom> symptoms = symptomRepository.findAllById(request.getSymptoms());
        List<SymptomDetails> symptomDetailsList = symptoms.stream()
                .map(symptom -> SymptomDetails.builder()
                        .symptomResult(symptomResult)
                        .symptom(symptom)
                        .build())
                .collect(Collectors.toList());
        symptomDetailsRepository.saveAll(symptomDetailsList);

        // Khi bệnh nhân hoặc bác sĩ submit xong một lần thì
        // SymptomResult là COMPLETED và DiagnosisSession chuyển sang PROCESSING.
        symptomResult.setStatus(SymptomResultStatus.COMPLETED);
        if (session.getStatus() == DiagnosisSessionStatus.PENDING) {
            session.setStatus(DiagnosisSessionStatus.PROCESSING);
        }

        symptomResultRepository.save(symptomResult);

        // Update weight, height trên session nếu bác sĩ thay đổi
        session.setWeight(request.getWeight());
        session.setHeight(request.getHeight());
        diagnosisSessionRepository.save(session);

        // Ghi log
        String action = "ROLE_PATIENT".equals(userRole) ? "PATIENT_SUBMIT" : "DOCTOR_SUBMIT";
        String description = "ROLE_PATIENT".equals(userRole)
                ? "Bệnh nhân điền biểu mẫu triệu chứng"
                : "Bác sĩ điền hộ biểu mẫu triệu chứng";
        systemLogService.logActivity("SymptomResult", symptomResult.getSymptomResultId(), action, description);

        // Emit event
        if ("ROLE_PATIENT".equals(userRole)) {
            eventPublisher.publishEvent(new SymptomFormSubmittedEvent(sessionId, userId));
        }

        return mapSymptomResultToResponse(symptomResult);
    }

    @Override
    public DiagnosisSessionResponse getSessionDetail(Integer sessionId) {
        DiagnosisSession session = diagnosisSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiên khám không tồn tại"));
        return mapToResponse(session);
    }

    @Override
    public SymptomResultResponse getSymptomResult(Integer sessionId) {
        SymptomResult symptomResult = symptomResultRepository.findByDiagnosisSessionSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Kết quả triệu chứng không tồn tại"));
        return mapSymptomResultToResponse(symptomResult);
    }

    private DiagnosisSessionResponse mapToResponse(DiagnosisSession session) {
        SymptomResultStatus symptomStatus = session.getSymptomResult() != null
                ? session.getSymptomResult().getStatus()
                : SymptomResultStatus.PENDING;

        return DiagnosisSessionResponse.builder()
                .sessionId(session.getSessionId())
                .patientId(session.getPatient().getPatientId())
                .patientName(session.getPatient().getUser().getFullName())
                .weight(session.getWeight())
                .height(session.getHeight())
                .status(session.getStatus())
                .symptomResultStatus(symptomStatus)
                .clinicalInputMode(session.getClinicalInputMode())
                .createdAt(session.getCreatedAt())
                .symptomResult(session.getSymptomResult() != null ? mapSymptomResultToResponse(session.getSymptomResult()) : null)
                .build();
    }

    private SymptomResultResponse mapSymptomResultToResponse(SymptomResult symptomResult) {
        List<SymptomDetailResponse> symptomDetails = symptomResult.getSymptomDetails() != null
                ? symptomResult.getSymptomDetails().stream()
                .map(sd -> SymptomDetailResponse.builder()
                        .symptomDetailId(sd.getSymptomDetailsId())
                        .symptomId(sd.getSymptom().getSymptomId())
                        .symptomName(sd.getSymptom().getSymptomName())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        List<Integer> symptomIds = symptomDetails.stream()
                .map(SymptomDetailResponse::getSymptomId)
                .collect(Collectors.toList());

        return SymptomResultResponse.builder()
                .symptomResultId(symptomResult.getSymptomResultId())
                .sessionId(symptomResult.getDiagnosisSession().getSessionId())
                .status(symptomResult.getStatus())
                .createdAt(symptomResult.getCreatedAt())
                .symptomIds(symptomIds)
                .menopauseStatus(symptomResult.getMenopauseStatus())
                .symptomDuration(symptomResult.getSymptomDuration())
                .symptomProgressing(symptomResult.getSymptomProgressing())
                .symptomDetails(symptomDetails)
                .build();
    }

    // Inner event classes
    public static class DiagnosisSessionCreatedEvent {
        private Integer sessionId;
        private Integer patientUserId;

        public DiagnosisSessionCreatedEvent(Integer sessionId, Integer patientUserId) {
            this.sessionId = sessionId;
            this.patientUserId = patientUserId;
        }

        public Integer getSessionId() {
            return sessionId;
        }

        public Integer getPatientUserId() {
            return patientUserId;
        }
    }

    public static class SymptomFormSubmittedEvent {
        private Integer sessionId;
        private Integer userId;

        public SymptomFormSubmittedEvent(Integer sessionId, Integer userId) {
            this.sessionId = sessionId;
            this.userId = userId;
        }

        public Integer getSessionId() {
            return sessionId;
        }

        public Integer getUserId() {
            return userId;
        }
    }
}
