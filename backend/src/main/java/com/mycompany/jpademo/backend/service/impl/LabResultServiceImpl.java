package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateLabResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.entity.LabResultParameter;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.LabResultParameterRepository;
import com.mycompany.jpademo.backend.repository.LabResultRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.LabResultService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link LabResultService}. Enforces ownership
 * rules (a lab order belongs to the session's assigned doctor) and
 * data-visibility rules (patients can only see their own, already
 * shared, sessions).
 */
@Service
@RequiredArgsConstructor
public class LabResultServiceImpl implements LabResultService {

    private final DiagnosisSessionRepository sessionRepository;

    private final LabResultRepository labResultRepository;

    private final LabResultParameterRepository labResultParameterRepository;

    private final SystemLogService systemLogService;

    private final LisMockDataProvider lisMockDataProvider;

    /**
     * Creates a new PENDING lab order for the given session.
     * Validation order: session exists -> caller is the owning doctor
     * -> testType is one of the supported types -> session is not
     * already COMPLETED -> no duplicate order of the same test type
     * already exists for this session.
     */
    @Override
    @Transactional
    public LabResultResponse createLabResult(CreateLabResultRequest request) {

        DiagnosisSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiên khám với ID: " + request.getSessionId()));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) auth.getPrincipal();
        Integer currentDoctorId = currentUser.getUser().getUserId();

        if (!session.getUser().getUserId().equals(currentDoctorId)) {
            throw new UnauthorizedActionException(
                    "Bạn không có quyền tạo xét nghiệm cho phiên khám này");
        }

        // chặn testType rác không nằm trong danh sách hỗ trợ
        if (!lisMockDataProvider.getSupportedTestTypes().contains(request.getTestType())) {
            throw new BadRequestException(
                    "Loại xét nghiệm không hợp lệ: \"" + request.getTestType() + "\"");
        }

        if (session.getStatus() == DiagnosisSessionStatus.COMPLETED) {
            throw new BadRequestException(
                    "Không thể tạo xét nghiệm cho phiên khám đã hoàn thành");
        }

        boolean alreadyExists = labResultRepository
                .existsByDiagnosisSession_SessionIdAndTestType(request.getSessionId(), request.getTestType());

        if (alreadyExists) {
            throw new BadRequestException(
                    "Phiên khám này đã có xét nghiệm loại \"" + request.getTestType() + "\". " +
                            "Vui lòng chọn loại xét nghiệm khác.");
        }

        LabResult labResult = LabResult.builder()
                .diagnosisSession(session)
                .testType(request.getTestType())
                .build();

        try {
            labResult = labResultRepository.save(labResult);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Two concurrent requests raced past the existsBy... check above;
            // the DB-level UNIQUE constraint is the final authority here.
            throw new BadRequestException(
                    "Phiên khám này đã có xét nghiệm loại \"" + request.getTestType() + "\". " +
                            "Vui lòng chọn loại xét nghiệm khác.");
        }

        systemLogService.logActivity("LabResult", labResult.getLabResultId(), "CREATE_LAB_RESULT",
                "Bác sĩ tạo chỉ định xét nghiệm \"" + labResult.getTestType()
                        + "\" cho phiên khám #" + session.getSessionId());

        return mapToLabResultResponse(labResult, Collections.emptyList());
    }

    /**
     * Returns all lab results for a session, resolving each result's
     * parameter values. PATIENT callers must own the session and the
     * session must be shared; DOCTOR callers are allowed to view any
     * session (cross-doctor consultation); every other role is
     * rejected outright.
     */
    @Override
    public List<LabResultResponse> getLabResultsBySession(Integer sessionId) {

        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiên khám với ID: " + sessionId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) auth.getPrincipal();
        String role = currentUser.getUser().getRole().getRoleName().name();
        Integer currentUserId = currentUser.getUser().getUserId();

        if (role.equals("PATIENT")) {
            // Kiểm tra phiên khám có thuộc về bệnh nhân này không
            Integer ownerUserId = session.getPatient().getUser().getUserId();
            if (!ownerUserId.equals(currentUserId)) {
                throw new UnauthorizedActionException(
                        "Bạn không có quyền xem xét nghiệm của phiên khám này");
            }

            // Kiểm tra isShared — nếu false thì chặn bệnh nhân
            if (!Boolean.TRUE.equals(session.getIsShared())) {
                throw new UnauthorizedActionException(
                        "Phiên khám này chưa được chia sẻ với bệnh nhân");
            }
        } else if (!role.equals("DOCTOR")) {
            // DOCTOR được phép xem chéo (hội chẩn/bàn giao ca) — chỉ chặn
            // các role không liên quan đến chuyên môn y tế: ADMIN, PHARMACIST,
            // RECEPTIONIST, ULTRASOUND_DOCTOR
            throw new UnauthorizedActionException(
                    "Vai trò của bạn không được phép xem xét nghiệm y tế của bệnh nhân");
        }

        List<LabResult> labResults = labResultRepository.findByDiagnosisSessionSessionId(sessionId);

        if (labResults == null || labResults.isEmpty()) {
            return Collections.emptyList();
        }

        return labResults.stream()
                .map(lr -> {
                    List<LabResultParameter> params =
                            labResultParameterRepository.findByLabResultLabResultId(lr.getLabResultId());
                    return mapToLabResultResponse(lr, params);
                })
                .collect(Collectors.toList());
    }

    /**
     * Maps a LabResult entity (plus its already-loaded parameters, if
     * any) to the response DTO used by the UI.
     */
    private LabResultResponse mapToLabResultResponse(LabResult labResult,
                                                     List<LabResultParameter> parameters) {

        List<LabResultResponse.ParameterValueResponse> paramResponses = new ArrayList<>();

        if (parameters != null) {

            paramResponses = parameters.stream().map(lrp -> {
                LabResultResponse.ParameterValueResponse.ParameterValueResponseBuilder builder =
                        LabResultResponse.ParameterValueResponse.builder()
                                .labResultParameterId(lrp.getLabResultParameterId())
                                .value(lrp.getValue());

                if (lrp.getParameter() != null) {
                    builder.parameterId(lrp.getParameter().getParameterId())
                            .parameterName(lrp.getParameter().getParameterName())
                            .unit(lrp.getParameter().getUnit());
                }

                return builder.build();

            }).collect(Collectors.toList());
        }

        return LabResultResponse.builder()
                .labResultId(labResult.getLabResultId())
                .sessionId(labResult.getDiagnosisSession() != null
                        ? labResult.getDiagnosisSession().getSessionId()
                        : null)
                .testType(labResult.getTestType())
                .status(labResult.getStatus() != null
                        ? labResult.getStatus()
                        : null)
                .createdAt(labResult.getCreatedAt() != null
                        ? labResult.getCreatedAt()
                        : null)
                .parameters(paramResponses)
                .build();
    }

    /**
     * Deletes a PENDING lab order belonging to the caller's session.
     * Also removes any (normally non-existent, since PENDING orders
     * have no results yet) LabResultParameter rows first to satisfy
     * the FK constraint.
     */
    @Override
    public void deleteLabResult(Integer labResultId) {

        LabResult labResult = labResultRepository.findById(labResultId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy xét nghiệm với ID: " + labResultId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) auth.getPrincipal();
        Integer currentDoctorId = currentUser.getUser().getUserId();

        DiagnosisSession session = labResult.getDiagnosisSession();

        if (!session.getUser().getUserId().equals(currentDoctorId)) {
            throw new UnauthorizedActionException(
                    "Bạn không có quyền xóa xét nghiệm của phiên khám này");
        }

        if (labResult.getStatus() != LabResultStatus.PENDING) {
            throw new BadRequestException(
                    "Chỉ có thể xóa xét nghiệm đang ở trạng thái Chờ xử lý");
        }

        // Xóa các tham số liên quan trước (phòng trường hợp phát sinh dữ liệu bất
        // thường), dù về nghiệp vụ PENDING vốn dĩ chưa có tham số nào.
        List<LabResultParameter> params =
                labResultParameterRepository.findByLabResultLabResultId(labResultId);
        if (params != null && !params.isEmpty()) {
            labResultParameterRepository.deleteAll(params);
        }

        labResultRepository.delete(labResult);

        systemLogService.logActivity("LabResult", labResult.getLabResultId(), "DELETE_LAB_RESULT",
                "Bác sĩ xóa chỉ định xét nghiệm \"" + labResult.getTestType()
                        + "\" cho phiên khám #" + session.getSessionId());
    }
}
