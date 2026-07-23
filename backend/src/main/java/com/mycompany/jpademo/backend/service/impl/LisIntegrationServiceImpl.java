package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.entity.LabResultParameter;
import com.mycompany.jpademo.backend.entity.Parameter;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.LabResultParameterRepository;
import com.mycompany.jpademo.backend.repository.LabResultRepository;
import com.mycompany.jpademo.backend.repository.ParameterRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.LisIntegrationService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link LisIntegrationService}. Simulates the
 * "software-level integration" layer between an external LIS/HIS
 * system and this application: turns a PENDING LabResult into a
 * COMPLETED one by saving its parameter values, auto-registering any
 * previously-unknown Parameter names into the shared catalog.
 */
@Service
@RequiredArgsConstructor
public class LisIntegrationServiceImpl implements LisIntegrationService {

    private final LabResultRepository labResultRepository;
    private final LabResultParameterRepository labResultParameterRepository;
    private final ParameterRepository parameterRepository;
    private final SystemLogService systemLogService;

    /**
     * Core result-ingestion logic shared by the real webhook and the
     * UI simulate button.
     * Steps: look up the PENDING LabResult (guards against double
     * completion) -> if source == UI_SIMULATE, verify the caller is
     * the session's owning doctor -> for each incoming item, resolve
     * or auto-create its Parameter catalog entry -> save one
     * LabResultParameter per item -> flip the LabResult to COMPLETED.
     */
    @Override
    @Transactional
    public LabResultResponse receiveLabResults(LisResultRequest request) {

        LabResult labResult = labResultRepository
                .findByLabResultIdAndStatus(request.getLabResultId(), LabResultStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy xét nghiệm đang chờ kết quả (PENDING) với labResultId: "
                                + request.getLabResultId()
                                + ". Có thể đã được xử lý hoặc labResultId không tồn tại."));

        // NEW: chỉ còn 1 caller duy nhất (UI simulate) nên luôn bắt buộc kiểm tra
        // quyền sở hữu — không còn nhánh "REAL_LIS" bỏ qua check này nữa.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) auth.getPrincipal();
        Integer currentDoctorId = currentUser.getUser().getUserId();
        Integer ownerDoctorId = labResult.getDiagnosisSession().getUser().getUserId();
        if (!ownerDoctorId.equals(currentDoctorId)) {
            throw new UnauthorizedActionException(
                    "Bạn không có quyền thao tác trên xét nghiệm của phiên khám này");
        }

        List<LabResultParameter> savedParameters = new ArrayList<>();

        for (LisResultRequest.TestResultItem item : request.getTestResults()) {
            Parameter parameter = parameterRepository
                    .findByParameterNameIgnoreCase(item.getTestName())
                    .orElseGet(() -> {
                        Parameter newParam = Parameter.builder()
                                .parameterName(item.getTestName())
                                .unit(item.getUnit() != null ? item.getUnit() : null)
                                .build();
                        return parameterRepository.save(newParam);
                    });

            LabResultParameter lrp = LabResultParameter.builder()
                    .labResult(labResult)
                    .parameter(parameter)
                    .value(item.getResultValue())
                    .build();

            savedParameters.add(labResultParameterRepository.save(lrp));
        }

        labResult.setStatus(LabResultStatus.COMPLETED);
        labResultRepository.save(labResult);

        // NEW: chỉ còn 1 loại action, bỏ nhánh phân biệt LIS_SIMULATE / LIS_RECEIVE
        systemLogService.logActivity("LabResult", labResult.getLabResultId(), "LIS_SIMULATE",
                "Bác sĩ mô phỏng lấy kết quả LIS cho xét nghiệm \"" + labResult.getTestType() + "\"");

        return mapToLabResultResponse(labResult, savedParameters);
    }

    /** Maps the now-COMPLETED LabResult and its freshly saved parameters to the response DTO. */
    private LabResultResponse mapToLabResultResponse(LabResult labResult,
                                                      List<LabResultParameter> parameters) {

        List<LabResultResponse.ParameterValueResponse> paramResponses = parameters.stream()
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
                .sessionId(labResult.getDiagnosisSession() != null
                        ? labResult.getDiagnosisSession().getSessionId()
                        : null)
                .testType(labResult.getTestType())
                .status(labResult.getStatus())
                .createdAt(labResult.getCreatedAt())
                .parameters(paramResponses)
                .build();
    }
}

