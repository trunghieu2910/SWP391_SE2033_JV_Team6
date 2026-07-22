package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.DoctorActionLog;
import com.mycompany.jpademo.backend.dto.request.CreateReviewRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateClinicalSymptomsRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: GiangLTHE194888
 * Task: Service implementation for managing doctor-side diagnosis sessions, handling symptoms, reviews, clinical modes, and medical images.
 */
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
    private final DiseaseTypeRepository diseaseTypeRepository;

    private static final String MESSAGE = "Ca chẩn đoán không tìm thấy với ID: ";


    private static final Map<DiagnosisSessionStatus, Set<DiagnosisSessionStatus>> ALLOWED_TRANSITIONS = Map.of(
            DiagnosisSessionStatus.PENDING,    EnumSet.of(DiagnosisSessionStatus.PROCESSING, DiagnosisSessionStatus.FAILED),
            DiagnosisSessionStatus.PROCESSING, EnumSet.of(DiagnosisSessionStatus.COMPLETED, DiagnosisSessionStatus.FAILED),
            DiagnosisSessionStatus.COMPLETED,  EnumSet.of(DiagnosisSessionStatus.PROCESSING),
            DiagnosisSessionStatus.FAILED,     EnumSet.of(DiagnosisSessionStatus.PENDING, DiagnosisSessionStatus.PROCESSING)
    );

    private static final Map<String, String> SYSTEMIC_SYMPTOM_NAMES = Map.of(
            "weightLoss", "Sụt cân không rõ nguyên nhân",
            "fatigue", "Mệt mỏi kéo dài",
            "anorexia", "Chán ăn"
    );

    private static final Map<String, String> RISK_FACTOR_NAMES = Map.of(
            "familyHistory", "Tiền sử gia đình ung thư phụ khoa",
            "obesity", "Béo phì",
            "diabetes", "Đái tháo đường",
            "hypertension", "Tăng huyết áp",
            "pcos", "Hội chứng buồng trứng đa nang (PCOS)",
            "estrogenTherapy", "Điều trị hormone estrogen kéo dài"
    );

    /** Retrieves doctor diagnosis sessions filtered by keyword, status, and dates. */
    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSessionResponse> getSessionsByDoctor(Integer doctorId,
                                                           Pageable pageable,
                                                           String keyword,
                                                           DiagnosisSessionStatus status,
                                                           LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            startDate = LocalDate.now();
            endDate = LocalDate.now();
        }

        if (startDate == null) {
            startDate = endDate;
        }

        if (endDate == null) {
            endDate = startDate;
        }

        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<DiagnosisSession> sessionPage = sessionRepository.searchByDoctorWithKeywordAndStatus(
                doctorId, normalizedKeyword, status, startDateTime, endDateTime, pageable);

        return sessionPage.map(session -> DoctorSessionResponse.builder()
                .sessionId(session.getSessionId())
                .patientId(session.getPatient().getPatientId())
                .fullName(session.getPatient().getUser() != null ? session.getPatient().getUser().getFullName() : "Unknown")
                .status(session.getStatus())
                .isShared(session.getIsShared())
                .createdAt(session.getCreatedAt())
                .build());
    }

    /** Updates the status of a specific diagnosis session. */
    @Override
    @Transactional
    @DoctorActionLog(action = "UPDATE_SESSION_STATUS", targetType = "DiagnosisSession")
    public void updateSessionStatus(Integer doctorId, UpdateSessionStatusRequest request) {
        DiagnosisSessionStatus newStatus = request.getStatus();
        if (newStatus == null) {
            throw new BadRequestException("Trạng thái không thể bỏ trống.");
        }
        Integer sessionId = request.getSessionId();
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));

        if (session.getReview() == null && DiagnosisSessionStatus.COMPLETED.equals(newStatus)) {
            throw new BadRequestException("Không thể chuyển sang trạng thái hoàn thành khi chưa có kết luận cuối cùng.");
        }
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật trạng thái ca chẩn đoán này.");
        }
        if (Boolean.TRUE.equals(session.getIsShared())) {
            throw new BadRequestException("Ca chẩn đoán đã được chia sẻ. Không thể cập nhật trạng thái.");
        }

        DiagnosisSessionStatus currentStatus = session.getStatus();
        if (currentStatus.equals(newStatus)) {
            throw new BadRequestException("Trạng thái mới đang trùng với trạng thái hiện tại.");
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
            throw new BadRequestException("Không thể chuyển trạng thái từ " + currentStatus + " sang " + newStatus + ".");
        }

        session.setStatus(newStatus);
        sessionRepository.save(session);
    }

    /** Updates the sharing configuration of a diagnosis session. */
    @Override
    @Transactional
    @DoctorActionLog(action = "UPDATE_SESSION_SHARE", targetType = "DiagnosisSession")
    public void updateSessionShare(Integer doctorId, UpdateSessionShareRequest request) {
        Integer sessionId = request.getSessionId();
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật trạng thái ca chẩn đoán này.");
        }
        if (session.getReview() == null) {
            throw new BadRequestException("Không thể công bố ca chẩn đoán chưa có kết luận cuối cùng.");
        }
        if (!DiagnosisSessionStatus.COMPLETED.equals(session.getStatus())) {
            throw new BadRequestException("Không thể công bố ca chẩn đoán chưa hoàn thành.");
        }
        session.setIsShared(request.getIsShared());
        sessionRepository.save(session);
    }

    /** Retrieves all symptoms recorded for a specific diagnosis session. */
    @Override
    @Transactional(readOnly = true)
    public List<SymptomResponse> getSessionSymptoms(Integer sessionId, Integer doctorId) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền truy cập vào ca chẩn đoán này.");
        }
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

    /** Saves or updates clinical symptoms associated with a diagnosis session. */
    @Override
    @Transactional
    public void updateClinicalSymptoms(Integer doctorId, Integer sessionId, UpdateClinicalSymptomsRequest request) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật triệu chứng của ca chẩn đoán này.");
        }

        if (DiagnosisSessionStatus.COMPLETED.equals(session.getStatus())) {
            throw new BadRequestException("Không thể cập nhật trạng thái của ca chẩn đoán này vì ca chẩn đoán đã hoàn thành.");
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

        Set<String> selectedSymptomNames = new HashSet<>();
        collectSelectedSymptomNames(request.getSystemicSymptoms(), SYSTEMIC_SYMPTOM_NAMES, selectedSymptomNames);
        collectSelectedSymptomNames(request.getRiskFactors(), RISK_FACTOR_NAMES, selectedSymptomNames);

        List<Symptom> nameBasedSymptoms = selectedSymptomNames.isEmpty()
                ? List.of()
                : symptomRepository.findBySymptomNameIn(selectedSymptomNames);

        if (nameBasedSymptoms.size() != selectedSymptomNames.size()) {
            Set<String> foundNames = nameBasedSymptoms.stream().map(Symptom::getSymptomName).collect(Collectors.toSet());
            selectedSymptomNames.removeAll(foundNames);
            throw new ResourceNotFoundException("Không tìm thấy triệu chứng trong DB: " + selectedSymptomNames);
        }

        if (symptomResult.getSymptomDetails() != null) {
            symptomResult.getSymptomDetails().clear();
        } else {
            symptomResult.setSymptomDetails(new ArrayList<>());
        }

        List<Symptom> idBasedSymptoms = allSymptomIds.isEmpty()
                ? List.of()
                : symptomRepository.findAllById(allSymptomIds);
        if (idBasedSymptoms.size() != allSymptomIds.size()) {
            throw new ResourceNotFoundException("Một số symptomId trong request không tồn tại trong DB.");
        }

        List<Symptom> finalSymptoms = new ArrayList<>(idBasedSymptoms);
        finalSymptoms.addAll(nameBasedSymptoms);

        for (Symptom symptom : finalSymptoms) {
            SymptomDetails detail = new SymptomDetails();
            detail.setSymptomResult(symptomResult);
            detail.setSymptom(symptom);
            symptomResult.getSymptomDetails().add(detail);
        }
        symptomResult.setStatus(SymptomResultStatus.COMPLETED);
        symptomResultRepository.save(symptomResult);

        if (session.getStatus() == DiagnosisSessionStatus.PENDING) {
            session.setStatus(DiagnosisSessionStatus.PROCESSING);
            sessionRepository.save(session);
        }
    }

    /** Configures clinical input mode for symptom selection. */
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

    /** Retrieves detailed session overview, patient data, and logs for a doctor. */
    @Override
    @Transactional
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
        Review review = reviewRepository.findByDiagnosisSessionSessionId(sessionId).orElse(null);

        // Tự động sửa trạng thái: nếu session đang PROCESSING nhưng đã có kết quả hình ảnh AI
        // thì chuyển sang PENDING (chờ bác sĩ kết luận)
        if (session.getStatus() == DiagnosisSessionStatus.PROCESSING && !medicalImages.isEmpty()) {
            session.setStatus(DiagnosisSessionStatus.PENDING);
            sessionRepository.save(session);
            log.info("Auto-fixed session {} status: PROCESSING -> PENDING (has {} medical images)",
                    sessionId, medicalImages.size());
        }

        return mapToDoctorSessionDetailResponse(session, patient, patientUser, symptomResult, labResults, medicalImages, review);
    }

    /** Saves the final review diagnosis and conclusions for a session. */
    @Override
    @Transactional
    public void saveReview(Integer doctorId, Integer sessionId, CreateReviewRequest request) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chẩn đoán với: " + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền cập nhật ca chẩn đoán này.");
        }

        if (session.getReview() != null) {
            throw new IllegalStateException("Ca chẩn đoán này đã có kết luận và không thể chỉnh sửa lại.");
        }



        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));

        Review review = new Review();
        review.setDiagnosisSession(session);
        review.setUser(doctor);

        DiseaseType diseaseType;
        if ("NEW".equals(request.getDiseaseTypeSelection())) {
            String newName = request.getNewDiseaseTypeName();
            if (newName == null || newName.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập tên loại bệnh mới");
            }
            String trimmedName = newName.trim();
            diseaseType = diseaseTypeRepository.findByNameIgnoreCase(trimmedName)
                    .orElseGet(() -> diseaseTypeRepository.save(
                            DiseaseType.builder().name(trimmedName).build()
                    ));
        } else {
            Integer diseaseTypeId;
            try {
                diseaseTypeId = Integer.parseInt(request.getDiseaseTypeSelection());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Loại bệnh không hợp lệ");
            }
            diseaseType = diseaseTypeRepository.findById(diseaseTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại bệnh"));
        }

        review.setDiseaseType(diseaseType);
        review.setTreatmentPlan(request.getTreatmentPlan());
        review.setDoctorAdvice(request.getDoctorAdvice());
        review.setNote(request.getNote());
        review.setReviewedAt(LocalDateTime.now());

        reviewRepository.save(review);

        session.setStatus(DiagnosisSessionStatus.COMPLETED);
        sessionRepository.save(session);
    }

    /** Extracts checked symptom names from request maps. */
    private void collectSelectedSymptomNames(Map<String, Boolean> source,
                                             Map<String, String> keyToSymptomName,
                                             Set<String> target) {
        if (source == null) return;
        keyToSymptomName.forEach((requestKey, symptomName) -> {
            if (Boolean.TRUE.equals(source.get(requestKey))) {
                target.add(symptomName);
            }
        });
    }

    /** Maps session components into a detailed session response DTO. */
    private DoctorSessionDetailResponse mapToDoctorSessionDetailResponse(
            DiagnosisSession session,
            Patient patient,
            User user,
            SymptomResult symptomResult,
            List<LabResult> labResults,
            List<MedicalImage> medicalImages,
            Review review) {

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

        ReviewResponse reviewResponse = null;
        if (review != null) {
            reviewResponse = ReviewResponse.builder()
                    .reviewId(review.getReviewId())
                    .sessionId(review.getDiagnosisSession().getSessionId())
                    .diseaseTypeId(review.getDiseaseType() != null ? review.getDiseaseType().getDiseaseTypeId() : null)
                    .finalDiagnosis(review.getDiseaseType() != null ? review.getDiseaseType().getName() : null)
                    .treatmentPlan(review.getTreatmentPlan())
                    .doctorAdvice(review.getDoctorAdvice())
                    .note(review.getNote())
                    .reviewedAt(review.getReviewedAt())
                    .build();
        }

        List<MedicalImageResponse> medicalImageResponses = medicalImages.stream()
                .map(this::mapToMedicalImageResponse)
                .collect(Collectors.toList());

        return DoctorSessionDetailResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus().name())
                .isShared(session.getIsShared())
                .createdAt(session.getCreatedAt())
                .weight(session.getWeight())
                .height(session.getHeight())
                .doctorId(session.getUser().getUserId())
                .doctorName(session.getUser().getFullName())
                .patientId(patient.getPatientId())
                .patientName(user.getFullName())
                .patientGender(patient.getGender())
                .clinicalInputMode(session.getClinicalInputMode())
                .patientDob(patient.getDob() != null ? patient.getDob().atStartOfDay() : null)
                .patientAddress(patient.getAddress())
                .symptomResult(symptomResultResponse)
                .labResults(labResultResponses)
                .medicalImages(medicalImageResponses)
                .review(reviewResponse)
                .build();
    }

    /** Maps medical image entity to DTO response. */
    private MedicalImageResponse mapToMedicalImageResponse(MedicalImage medicalImage) {
        List<MedicalImageDetailResponse> details = medicalImage.getMedicalImageDetailsList() != null ?
            medicalImage.getMedicalImageDetailsList().stream()
                .map(detail -> MedicalImageDetailResponse.builder()
                        .imageId(detail.getImageId())
                        .imageUrl(detail.getImageUrl())
                        .aiImageUrl(detail.getAiImageUrl())
                        .confidenceScore(detail.getConfidenceScore())
                        .ultrasoundConclusion(detail.getUltrasoundConclusion())
                        .imgResultConclusion(detail.getImgResultConclusion())
                        .aiImageUrl(detail.getAiImageUrl())
                        .imgResultConclusion(detail.getImgResultConclusion())
                        .uploadedAt(detail.getUploadedAt())
                        .build())
                .collect(Collectors.toList()) : List.of();

        return MedicalImageResponse.builder()
                .medicalImageId(medicalImage.getMedicalImageId())
                .sessionId(medicalImage.getDiagnosisSession().getSessionId())
                .imageType(medicalImage.getImageType())
                .status(medicalImage.getStatus())
                .createdAt(medicalImage.getCreatedAt())
                .images(details)
                .build();
    }

    /** Maps lab result entity to DTO response. */
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

    /** Deletes an imaging order from a diagnosis session. */
    @Override
    @Transactional
    @DoctorActionLog(action = "DELETE_MEDICAL_IMAGE", targetType = "MedicalImage")
    public void deleteMedicalImage(Integer doctorId, Integer sessionId, Integer imageId) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền thao tác trên ca chẩn đoán này.");
        }

        MedicalImage image = medicalImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hình ảnh y tế với ID: " + imageId));

        if (!image.getDiagnosisSession().getSessionId().equals(sessionId)) {
            throw new BadRequestException("Hình ảnh không thuộc ca chẩn đoán này.");
        }

        if (image.getStatus() != com.mycompany.jpademo.backend.enums.MedicalImageStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể xóa yêu cầu hình ảnh đang ở trạng thái Chờ xử lý.");
        }

        medicalImageRepository.delete(image);
    }

    /** Generates a new medical imaging order for a session. */
    @Override
    @Transactional
    @DoctorActionLog(action = "REQUEST_MEDICAL_IMAGE", targetType = "MedicalImage")
    public void createMedicalImage(Integer doctorId, Integer sessionId, String imageType) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền thao tác trên ca chẩn đoán này.");
        }

        if (imageType == null || imageType.trim().isEmpty()) {
            throw new BadRequestException("Loại hình ảnh y tế không được để trống.");
        }

        MedicalImage image = MedicalImage.builder()
                .diagnosisSession(session)
                .imageType(imageType.trim())
                .status(com.mycompany.jpademo.backend.enums.MedicalImageStatus.PENDING)
                .build();
        
        medicalImageRepository.save(image);
    }

    @Override
    public void verifyDoctorOwnsSession(Integer doctorId, Integer sessionId) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chẩn đoán với: " + sessionId));
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền thao tác trên ca chẩn đoán này.");
        }
    }
}