package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateReviewRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateClinicalSymptomsRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorDiagnosisServiceImpl implements DoctorDiagnosisService {

    private final DiagnosisSessionRepository sessionRepository;
    private final SymptomRepository symptomRepository;
    private final SymptomResultRepository symptomResultRepository;
    private final LabResultRepository labResultRepository;
    private final MedicalImageRepository medicalImageRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private static final String MESSAGE = "Ca chẩn đoán không tìm thấy với ID: ";

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSessionResponse> getSessionsByDoctor(Integer doctorId,
                                                           Pageable pageable,
                                                           String keyword,
                                                           DiagnosisSessionStatus status) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<DiagnosisSession> sessionPage = sessionRepository.searchByDoctorWithKeywordAndStatus(
                doctorId, normalizedKeyword, status, pageable);

        return sessionPage.map(session -> DoctorSessionResponse.builder()
                .sessionId(session.getSessionId())
                .patientId(session.getPatient().getPatientId())
                .fullName(session.getPatient().getUser() != null ? session.getPatient().getUser().getFullName() : "Unknown")
                .status(session.getStatus())
                .isShared(session.getIsShared())
                .createdAt(session.getCreatedAt())
                .build());
    }

    @Override
    @Transactional
    public void updateSessionStatus(Integer doctorId, UpdateSessionStatusRequest request) {
        Integer sessionId = request.getSessionId();
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật trạng thái ca chẩn đoán này.");
        }
        if (session.getIsShared() != null && session.getIsShared()) {
            throw new BadRequestException("Ca chẩn đoán đã được chia sẻ. Không thể cập nhật trạng thái.");
        }

        DiagnosisSessionStatus newStatus = request.getStatus();
        if (newStatus == null) {
            throw new BadRequestException("Trạng thái không thể bỏ trống.");
        }

        session.setStatus(newStatus);
        sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void updateSessionShare(Integer doctorId, UpdateSessionShareRequest request) {
        Integer sessionId = request.getSessionId();
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật trạng thái ca chẩn đoán này.");
        }
        if (!DiagnosisSessionStatus.COMPLETED.equals(session.getStatus())) {
            throw new BadRequestException("Không thể công bố ca chẩn đoán chưa hoàn thành.");
        }
        session.setIsShared(request.getIsShared());
        sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SymptomResponse> getSessionSymptoms(Integer sessionId) {
        SymptomResult symptomResult = symptomResultRepository
                .findByDiagnosisSessionSessionId(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy triệu chứng lâm sàng cho ca chẩn đoán ID: " + sessionId));
        String menopauseStatus = symptomResult.getMenopauseStatus() != null
                ? symptomResult.getMenopauseStatus()
                : null;
        String symptomDuration = symptomResult.getSymptomDuration();
        Boolean symptomProgressing = symptomResult.getSymptomProgressing();

        return symptomResult.getSymptomDetails()
                .stream()
                .map(detail -> SymptomResponse.builder()
                        .symptomId(detail.getSymptom().getSymptomId())
                        .symptomName(detail.getSymptom().getSymptomName())
                        .menopauseStatus(menopauseStatus)
                        .symptomDuration(symptomDuration)
                        .symptomProgressing(symptomProgressing)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateClinicalSymptoms(Integer doctorId, Integer sessionId, UpdateClinicalSymptomsRequest request) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật triệu chứng của ca chẩn đoán này.");
        }

        if (session.getClinicalInputMode() == ClinicalInputMode.PATIENT
                && (session.getSymptomResult() == null || session.getSymptomResult().getStatus() != SymptomResultStatus.COMPLETED)) {
            throw new BadRequestException("Phiên khám này yêu cầu bệnh nhân gửi triệu chứng trước khi bác sĩ có thể chỉnh sửa.");
        }

        if (request.getHeight() != null) session.setHeight(request.getHeight());
        if (request.getWeight() != null) session.setWeight(request.getWeight());
        sessionRepository.save(session);

        SymptomResult symptomResult = symptomResultRepository
                .findByDiagnosisSessionSessionId(sessionId)
                .orElseGet(() -> {
                    SymptomResult result = new SymptomResult();
                    result.setDiagnosisSession(session);
                    result.setStatus(SymptomResultStatus.COMPLETED);
                    result.setCreatedAt(LocalDateTime.now());
                    result.setSymptomDetails(new ArrayList<>());
                    return result;
                });

        symptomResult.setMenopauseStatus(request.getMenopauseStatus());
        symptomResult.setSymptomDuration(request.getSymptomDuration());
        symptomResult.setSymptomProgressing(request.getSymptomProgressing());

        List<Integer> allSymptomIds = new ArrayList<>();

        if (request.getAbnormalBleedingIds() != null) allSymptomIds.addAll(request.getAbnormalBleedingIds());
        if (request.getAbnormalDischargeIds() != null) allSymptomIds.addAll(request.getAbnormalDischargeIds());
        if (request.getPainIds() != null) allSymptomIds.addAll(request.getPainIds());
        if (request.getUrinarySymptomsIds() != null) allSymptomIds.addAll(request.getUrinarySymptomsIds());
        if (request.getDigestiveSymptomsIds() != null) allSymptomIds.addAll(request.getDigestiveSymptomsIds());

        if (request.getSystemicSymptoms() != null) {
            if (Boolean.TRUE.equals(request.getSystemicSymptoms().get("weightLoss"))) allSymptomIds.add(14);
            if (Boolean.TRUE.equals(request.getSystemicSymptoms().get("fatigue"))) allSymptomIds.add(15);
            if (Boolean.TRUE.equals(request.getSystemicSymptoms().get("anorexia"))) allSymptomIds.add(16);
        }

        if (request.getRiskFactors() != null) {
            if (Boolean.TRUE.equals(request.getRiskFactors().get("familyHistory"))) allSymptomIds.add(25);
            if (Boolean.TRUE.equals(request.getRiskFactors().get("obesity"))) allSymptomIds.add(26);
            if (Boolean.TRUE.equals(request.getRiskFactors().get("diabetes"))) allSymptomIds.add(27);
            if (Boolean.TRUE.equals(request.getRiskFactors().get("hypertension"))) allSymptomIds.add(28);
            if (Boolean.TRUE.equals(request.getRiskFactors().get("pcos"))) allSymptomIds.add(29);
            if (Boolean.TRUE.equals(request.getRiskFactors().get("estrogenTherapy"))) allSymptomIds.add(30);
        }

        if (symptomResult.getSymptomDetails() != null) {
            symptomResult.getSymptomDetails().clear();
        } else {
            symptomResult.setSymptomDetails(new ArrayList<>());
        }

        for (Integer symptomId : allSymptomIds) {
            Symptom symptom = symptomRepository.findById(symptomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Symptom not found: " + symptomId));

            SymptomDetails detail = new SymptomDetails();
            detail.setSymptomResult(symptomResult);
            detail.setSymptom(symptom);
            symptomResult.getSymptomDetails().add(detail);
        }
        // Mark symptom result as completed when doctor saves and set session to PROCESSING
        symptomResult.setStatus(SymptomResultStatus.COMPLETED);
        symptomResultRepository.save(symptomResult);

        if (session.getStatus() == DiagnosisSessionStatus.PENDING) {
            session.setStatus(DiagnosisSessionStatus.PROCESSING);
            sessionRepository.save(session);
        }
    }

    @Override
    @Transactional
    public void setClinicalInputMode(Integer doctorId, Integer sessionId, ClinicalInputMode clinicalInputMode) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền thay đổi chế độ nhập triệu chứng của ca chẩn đoán này.");
        }
        // Allow changing mode only if patient hasn't submitted yet
        if (session.getClinicalInputMode() != null && 
            session.getSymptomResult() != null && 
            session.getSymptomResult().getStatus() == SymptomResultStatus.COMPLETED) {
            throw new BadRequestException("Bệnh nhân đã gửi triệu chứng. Không thể thay đổi chế độ nhập.");
        }
        session.setClinicalInputMode(clinicalInputMode);
        sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorSessionDetailResponse getSessionDetail(Integer sessionId, Integer doctorId) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chẩn đoán với: " + sessionId));
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền xem ca chẩn đoán này.");
        }
        Patient patient = session.getPatient();
        User patientUser = patient.getUser();
        SymptomResult symptomResult = symptomResultRepository
                .findBySessionIdWithDetails(sessionId)
                .orElse(null);
        List<LabResult> labResults = labResultRepository.findByDiagnosisSessionSessionId(sessionId);
        List<MedicalImage> medicalImages = medicalImageRepository.findByDiagnosisSession_SessionId(sessionId);

        return mapToDoctorSessionDetailResponse(session, patient, patientUser, symptomResult, labResults, medicalImages);
    }

    @Override
    @Transactional
    public void saveReview(Integer doctorId, Integer sessionId, CreateReviewRequest request) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chẩn đoán với: " + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật ca chẩn đoán này.");
        }

        Review review = session.getReview();
        if (review == null) {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
            
            review = new Review();
            review.setDiagnosisSession(session);
            review.setUser(doctor);
        }

        review.setFinalDiagnosis(request.getFinalDiagnosis());
        review.setTreatmentPlan(request.getTreatmentPlan());
        review.setDoctorAdvice(request.getDoctorAdvice());
        review.setNote(request.getNote());
        review.setReviewedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }

    private DoctorSessionDetailResponse mapToDoctorSessionDetailResponse(
            DiagnosisSession session,
            Patient patient,
            User user,
            SymptomResult symptomResult,
            List<LabResult> labResults,
            List<MedicalImage> medicalImages) {

        SymptomResultResponse symptomResultResponse = null;
        if (symptomResult != null) {
            List<SymptomDetailResponse> symptomDetails = symptomResult.getSymptomDetails() != null ?
                    symptomResult.getSymptomDetails().stream()
                    .map(sd -> SymptomDetailResponse.builder()
                               .symptomDetailId(sd.getSymptomDetailsId())
                               .symptomId(sd.getSymptom().getSymptomId())
                               .symptomName(sd.getSymptom().getSymptomName())
                               .build())
                    .collect(Collectors.toList()) : null;

            symptomResultResponse = SymptomResultResponse.builder()
                    .symptomResultId(symptomResult.getSymptomResultId())
                    .sessionId(session.getSessionId())
                    .status(symptomResult.getStatus())
                    .createdAt(symptomResult.getCreatedAt())
                    .menopauseStatus(symptomResult.getMenopauseStatus())
                    .symptomDuration(symptomResult.getSymptomDuration())
                    .symptomProgressing(symptomResult.getSymptomProgressing())
                    .symptomDetails(symptomDetails)
                    .build();
        }

        List<LabResultResponse> labResultResponses = labResults.stream()
                .map(this::mapToLabResultResponse)
                .collect(Collectors.toList());

        List<MedicalImageResponse> medicalImageResponses = medicalImages.stream()
                .map(this::mapToMedicalImageResponse)
                .collect(Collectors.toList());

        ReviewResponse reviewResponse = null;
        if (session.getReview() != null) {
            Review rev = session.getReview();
            reviewResponse = ReviewResponse.builder()
                    .reviewId(rev.getReviewId())
                    .finalDiagnosis(rev.getFinalDiagnosis())
                    .treatmentPlan(rev.getTreatmentPlan())
                    .doctorAdvice(rev.getDoctorAdvice())
                    .note(rev.getNote())
                    .reviewedAt(rev.getReviewedAt())
                    .build();
        }

        return DoctorSessionDetailResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus() != null ? session.getStatus().name() : null)
                .isShared(session.getIsShared())
                .createdAt(session.getCreatedAt())
                .weight(session.getWeight())
                .height(session.getHeight())
                .doctorId(session.getUser().getUserId())
                .doctorName(session.getUser().getFullName())
                .patientId(patient.getPatientId())
                .patientName(user.getFullName())
                .patientGender(patient.getGender())
                .patientDob(patient.getDob() != null ? patient.getDob().atStartOfDay() : null)
                .patientAddress(patient.getAddress())
                .clinicalInputMode(session.getClinicalInputMode())
                .symptomResult(symptomResultResponse)
                .labResults(labResultResponses)
                .medicalImages(medicalImageResponses)
                .review(reviewResponse)
                .build();
    }

    private MedicalImageResponse mapToMedicalImageResponse(MedicalImage medicalImage) {
        List<MedicalImageDetailResponse> details = medicalImage.getMedicalImageDetailsList().stream()
                .map(detail -> MedicalImageDetailResponse.builder()
                        .imageId(detail.getImageId())
                        .imageUrl(detail.getImageUrl())
                        .uploadedAt(detail.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        return MedicalImageResponse.builder()
                .medicalImageId(medicalImage.getMedicalImageId())
                .sessionId(medicalImage.getDiagnosisSession().getSessionId())
                .imageType(medicalImage.getImageType())
                .status(medicalImage.getStatus())
                .createdAt(medicalImage.getCreatedAt())
                .images(details)
                .build();
    }

    private LabResultResponse mapToLabResultResponse(LabResult labResult) {
        List<LabResultResponse.ParameterValueResponse> parameters = labResult.getLabResultParameters().stream()
                .map(lrp -> LabResultResponse.ParameterValueResponse.builder()
                        .labResultParameterId(lrp.getLabResultParameterId())
                        .parameterId(lrp.getParameter().getParameterId())
                        .parameterName(lrp.getParameter().getParameterName())
                        .unit(lrp.getParameter().getUnit())
                        .value(lrp.getValue())
                        .build())
                .collect(Collectors.toList());

        return LabResultResponse.builder()
                .labResultId(labResult.getLabResultId())
                .sessionId(labResult.getDiagnosisSession().getSessionId())
                .testType(labResult.getTestType())
                .status(labResult.getStatus())
                .createdAt(labResult.getCreatedAt())
                .parameters(parameters)
                .build();
    }
}