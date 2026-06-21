package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.aop.context.AuditLogContext;
import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.entity.LabResultParameter;
import com.mycompany.jpademo.backend.entity.Parameter;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.LabResultParameterRepository;
import com.mycompany.jpademo.backend.repository.LabResultRepository;
import com.mycompany.jpademo.backend.repository.ParameterRepository;
import com.mycompany.jpademo.backend.service.interfaces.LisIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mô phỏng tầng "Software-level Integration" (LIS/HIS → AI System).
 */
@Service
@RequiredArgsConstructor
public class LisIntegrationServiceImpl implements LisIntegrationService {

    private final LabResultRepository labResultRepository;
    private final LabResultParameterRepository labResultParameterRepository;
    private final ParameterRepository parameterRepository;

    @Override
    @Transactional
    @LogActivity(action = "RECEIVE_LAB_RESULTS", targetType = "LabResults", description = "Nhận kết quả xét nghiệm từ LIS")
    public LabResultResponse receiveLabResults(LisResultRequest request) {

        LabResult labResult = labResultRepository
                .findByLabResultIdAndStatus(request.getLabResultId(), LabResultStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy xét nghiệm đang chờ kết quả (PENDING) với labResultId: "
                                + request.getLabResultId()
                                + ". Có thể đã được xử lý hoặc labResultId không tồn tại."));

        List<LabResultParameter> savedParameters = new ArrayList<>();

        for (LisResultRequest.TestResultItem item : request.getTestResults()) {

            Parameter parameter = parameterRepository
                    .findByParameterNameIgnoreCase(item.getTestName())
                    .orElseGet(() -> {
                        Parameter newParam = Parameter.builder()
                                .parameterName(item.getTestName())
                                .unit(item.getUnit() != null ? item.getUnit() : "—")
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

        AuditLogContext.setTargetId(labResult.getLabResultId());

        return mapToLabResultResponse(labResult, savedParameters);
    }

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

