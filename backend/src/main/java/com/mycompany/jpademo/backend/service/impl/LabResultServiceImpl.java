package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateLabResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.entity.LabResultParameter;
import com.mycompany.jpademo.backend.entity.Parameter;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.LabResultParameterRepository;
import com.mycompany.jpademo.backend.repository.LabResultRepository;
import com.mycompany.jpademo.backend.repository.ParameterRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.LabResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabResultServiceImpl implements LabResultService {

    private final DiagnosisSessionRepository sessionRepository;

    private final LabResultRepository labResultRepository;

    private final LabResultParameterRepository labResultParameterRepository;

    private final ParameterRepository parameterRepository;

    @Override
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

        if (session.getStatus() == DiagnosisSessionStatus.COMPLETED) {
            throw new BadRequestException(
                    "Không thể tạo xét nghiệm cho phiên khám đã hoàn thành");
        }

        LabResult labResult = LabResult.builder()
                .diagnosisSession(session)
                .testType(request.getTestType())
                .build();

        labResult = labResultRepository.save(labResult);

        List<LabResultParameter> savedParameters = new ArrayList<>();

        if (request.getParameters() != null && !request.getParameters().isEmpty()) {

            List<Integer> paramIds = request.getParameters().stream()
                    .map(CreateLabResultRequest.ParameterValueRequest::getParameterId)
                    .collect(Collectors.toList());
            long distinctCount = paramIds.stream().distinct().count();
            if (distinctCount < paramIds.size()) {
                throw new BadRequestException(
                        "Danh sách thông số xét nghiệm chứa ID bị trùng lặp");
            }

            for (CreateLabResultRequest.ParameterValueRequest pvr : request.getParameters()) {

                Parameter parameter = parameterRepository.findById(pvr.getParameterId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy thông số xét nghiệm với ID: " + pvr.getParameterId()));

                LabResultParameter lrp = LabResultParameter.builder()
                        .labResult(labResult)
                        .parameter(parameter)
                        .value(pvr.getValue())
                        .build();

                savedParameters.add(labResultParameterRepository.save(lrp));
            }
        }

        return mapToLabResultResponse(labResult, savedParameters);
    }

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
                        ? Timestamp.valueOf(labResult.getCreatedAt())
                        : null)
                .parameters(paramResponses)
                .build();
    }
}
