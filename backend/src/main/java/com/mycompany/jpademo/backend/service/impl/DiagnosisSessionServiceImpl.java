package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.DoctorWorkloadResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomDetailResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.enums.MedicalImageStatus;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import com.mycompany.jpademo.backend.dto.request.CreatePatientSessionRequest;

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
    public DiagnosisSessionResponse createSession(CreateDiagnosisSessionRequest request, Integer creatorId) {
        // Kiểm tra bệnh nhân tồn tại
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Bệnh nhân không tồn tại"));

        // Kiểm tra bác sĩ tồn tại
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));

        // Kiểm tra business rule: Bệnh nhân đã có phiên khám nào chưa, nếu phiên gần nhất chưa COMPLETED thì chặn
        List<DiagnosisSession> existingSessions = diagnosisSessionRepository.findByPatientPatientId(patient.getPatientId());
        if (!existingSessions.isEmpty()) {
            existingSessions.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt())); // Descending
            DiagnosisSession latestSession = existingSessions.get(0);
            if (latestSession.getStatus() != DiagnosisSessionStatus.COMPLETED) {
                throw new BadRequestException("Bệnh nhân này đang có một phiên khám chưa hoàn thành (Mã phiên: " + latestSession.getSessionId() + "). Không thể tạo mới.");
            }
        }

        // Kiểm tra business rule: Bác sĩ không được có quá 10 ca chẩn đoán chưa hoàn tất
        long incompleteDoctorSessions = diagnosisSessionRepository.countByUserUserIdAndStatusNot(doctor.getUserId(), DiagnosisSessionStatus.COMPLETED);
        if (incompleteDoctorSessions >= 10) {
            throw new BadRequestException("Bác sĩ này đang có " + incompleteDoctorSessions + " ca chẩn đoán chưa hoàn tất. Không thể nhận thêm ca mới (Tối đa 10 ca).");
        }
        
        // Lấy thông tin người tạo (Lễ tân)
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Người tạo không tồn tại"));

        // Tạo DiagnosisSession
        DiagnosisSession session = DiagnosisSession.builder()
                .patient(patient)
                .user(doctor)
                .weight(request.getWeight())
                .height(request.getHeight())
                .status(DiagnosisSessionStatus.PENDING)
                .clinicalInputMode(request.getClinicalInputMode())
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



        // Ghi log
        systemLogService.logActivity("DiagnosisSession", savedSession.getSessionId(), "CREATE",
                "Lễ tân " + creator.getFullName() + " tạo phiên khám mới cho bệnh nhân " + patient.getUser().getFullName() + ", chỉ định bác sĩ " + doctor.getFullName());

        // Emit event để notify patient
        eventPublisher.publishEvent(new DiagnosisSessionCreatedEvent(savedSession.getSessionId(), patient.getUser().getUserId()));

        return mapToResponse(savedSession);
    }
// The Hieu
    @Override
    @Transactional
    public DiagnosisSessionResponse addPatientToSession(CreateDiagnosisSessionRequest request, Integer creatorId) {
        // Tương tự như createSession
        return createSession(request, creatorId);
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

    @Override
    @Transactional
    public DiagnosisSessionResponse createSessionForPatient(CreatePatientSessionRequest request, User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy patient cho user"));

        DiagnosisSession session = DiagnosisSession.builder()
                .patient(patient)
                .user(patient.getUser())
                .weight(request != null ? request.getWeight() : null)
                .height(request != null ? request.getHeight() : null)
                .status(DiagnosisSessionStatus.PENDING)
                .build();

        DiagnosisSession saved = diagnosisSessionRepository.save(session);

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

        return DiagnosisSessionResponse.builder()
                .sessionId(saved.getSessionId())
                .patientId(patient.getPatientId())
                .patientName(patient.getUser().getFullName())
                .status(saved.getStatus())
                .symptomResultStatus(symptomResult.getStatus())
                .clinicalInputMode(saved.getClinicalInputMode())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    public DiagnosisSessionResponse getActiveSessionForPatient(User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy patient cho user"));

        var sessions = diagnosisSessionRepository.findByPatientPatientId(patient.getPatientId());
        var active = sessions.stream()
                .filter(s -> s.getStatus() != DiagnosisSessionStatus.COMPLETED)
                .max(Comparator.comparing(DiagnosisSession::getCreatedAt))
                .orElse(null);

        if (active != null) {
            return DiagnosisSessionResponse.builder()
                    .sessionId(active.getSessionId())
                    .patientId(active.getPatient().getPatientId())
                    .patientName(active.getPatient().getUser().getFullName())
                    .status(active.getStatus())
                    .symptomResultStatus(active.getSymptomResult() != null ? active.getSymptomResult().getStatus() : null)
                    .createdAt(active.getCreatedAt())
                    .build();
        }
        return null;
    }

    @Override
    public List<DiagnosisSessionResponse> getSessionsForPatient(User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy patient cho user"));

        var sessions = diagnosisSessionRepository.findByPatientPatientId(patient.getPatientId());

        return sessions.stream().map(s -> DiagnosisSessionResponse.builder()
                .sessionId(s.getSessionId())
                .patientId(s.getPatient().getPatientId())
                .patientName(s.getPatient().getUser().getFullName())
                .status(s.getStatus())
                .symptomResultStatus(s.getSymptomResult() != null ? s.getSymptomResult().getStatus() : null)
                .clinicalInputMode(s.getClinicalInputMode())
                .createdAt(s.getCreatedAt())
                .build()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisSessionResponse> getPendingUltrasoundSessions() {
        var sessions = diagnosisSessionRepository.findByStatusInOrderByCreatedAtDesc(
            List.of(DiagnosisSessionStatus.PENDING, DiagnosisSessionStatus.PROCESSING)
        );
        // Lọc những session có ít nhất 1 hình ảnh siêu âm đang chờ xử lý (PENDING)
        var filteredSessions = sessions.stream().filter(s -> {
            if (s.getMedicalImages() != null) {
                for (var image : s.getMedicalImages()) {
                    if (image.getStatus() == MedicalImageStatus.PENDING) {
                        return true;
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
        
        return filteredSessions.stream().map(s -> {
            String types = "";
            if (s.getMedicalImages() != null) {
                types = s.getMedicalImages().stream()
                        .filter(img -> img.getStatus() == MedicalImageStatus.PENDING)
                        .map(MedicalImage::getImageType)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.joining(", "));
            }
            return DiagnosisSessionResponse.builder()
                .sessionId(s.getSessionId())
                .patientId(s.getPatient().getPatientId())
                .patientName(s.getPatient().getUser().getFullName())
                .patientCccd(s.getPatient().getUser().getNationalID())
                .status(s.getStatus())
                .symptomResultStatus(s.getSymptomResult() != null ? s.getSymptomResult().getStatus() : null)
                .clinicalInputMode(s.getClinicalInputMode())
                .createdAt(s.getCreatedAt())
                .imageType(types)
                .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisSessionResponse> getCompletedUltrasoundSessions() {
        var sessions = diagnosisSessionRepository.findByStatusInOrderByCreatedAtDesc(
            List.of(DiagnosisSessionStatus.PENDING, DiagnosisSessionStatus.PROCESSING, DiagnosisSessionStatus.COMPLETED)
        );
        
        return sessions.stream().filter(s -> {
            if (s.getMedicalImages() != null) {
                for (var image : s.getMedicalImages()) {
                    if (image.getStatus() == MedicalImageStatus.COMPLETED) {
                        return true;
                    }
                }
            }
            return false;
        }).map(s -> {
            Integer imageId = null;
            String types = "";
            if (s.getMedicalImages() != null) {
                for (var image : s.getMedicalImages()) {
                    if (image.getStatus() == MedicalImageStatus.COMPLETED) {
                        if (image.getMedicalImageDetailsList() != null && !image.getMedicalImageDetailsList().isEmpty()) {
                            imageId = image.getMedicalImageDetailsList().get(0).getImageId();
                        }
                    }
                }
                
                types = s.getMedicalImages().stream()
                        .filter(img -> img.getStatus() == MedicalImageStatus.COMPLETED)
                        .map(MedicalImage::getImageType)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.joining(", "));
            }
            return DiagnosisSessionResponse.builder()
                .sessionId(s.getSessionId())
                .patientId(s.getPatient().getPatientId())
                .patientName(s.getPatient().getUser().getFullName())
                .patientCccd(s.getPatient().getUser().getNationalID())
                .status(s.getStatus())
                .symptomResultStatus(s.getSymptomResult() != null ? s.getSymptomResult().getStatus() : null)
                .clinicalInputMode(s.getClinicalInputMode())
                .createdAt(s.getCreatedAt())
                .medicalImageDetailsId(imageId)
                .imageType(types)
                .build();
        }).collect(Collectors.toList());
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

    // ==================== DOCTOR WORKLOAD (for Receptionist) ====================

    // [Nguyen The Hieu]: Bước 3 - Service Impl: Triển khai logic tính toán danh sách tải bác sĩ (Màn hình 1)
    @Override
    public List<DoctorWorkloadResponse> getDoctorWorkloads() {
        // Lấy tất cả bác sĩ đang có trạng thái ACTIVE
        List<User> doctors = userRepository.findByRoleRoleNameAndStatus(
                RoleName.DOCTOR, UserStatus.ACTIVE, Pageable.unpaged()).getContent();

        // Với mỗi bác sĩ, gọi repository để đếm số lượng ca theo từng trạng thái (PENDING, PROCESSING, FAILED)
        return doctors.stream().map(doctor -> {
            long pending = diagnosisSessionRepository.countByUserUserIdAndStatus(
                    doctor.getUserId(), DiagnosisSessionStatus.PENDING);
            long processing = diagnosisSessionRepository.countByUserUserIdAndStatus(
                    doctor.getUserId(), DiagnosisSessionStatus.PROCESSING);
            long failed = diagnosisSessionRepository.countByUserUserIdAndStatus(
                    doctor.getUserId(), DiagnosisSessionStatus.FAILED);
            // Đếm tổng số ca ĐANG HOẠT ĐỘNG (nghĩa là trạng thái khác COMPLETED)
            long totalActive = diagnosisSessionRepository.countByUserUserIdAndStatusNot(
                    doctor.getUserId(), DiagnosisSessionStatus.COMPLETED);

            // Gói vào DTO và trả về
            return DoctorWorkloadResponse.builder()
                    .doctorId(doctor.getUserId())
                    .doctorName(doctor.getFullName())
                    .pendingCount(pending)
                    .processingCount(processing)
                    .failedCount(failed)
                    .totalActive(totalActive)
                    .build();
        }).collect(Collectors.toList());
    }

    // [Nguyen The Hieu]: Bước 3 - Service Impl: Triển khai logic lấy chi tiết ca khám của một bác sĩ (Màn hình 2)
    @Override
    public Page<DiagnosisSession> getSessionsByDoctor(Integer doctorId,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      Pageable pageable) {
        // Chỉ đơn giản là gọi xuống Repository method vừa tạo ở Bước 1
        return diagnosisSessionRepository.findByDoctorIdWithDateFilter(
                doctorId, startDate, endDate, pageable);
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
