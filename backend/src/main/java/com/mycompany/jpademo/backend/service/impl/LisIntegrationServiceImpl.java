package com.mycompany.jpademo.backend.service.impl;

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
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
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
    private final SystemLogService systemLogService;

    @Override
    @Transactional
    public LabResultResponse receiveLabResults(LisResultRequest request, String source) {

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

        String action = "UI_SIMULATE".equals(source) ? "LIS_SIMULATE" : "LIS_RECEIVE";
        String description = "UI_SIMULATE".equals(source)
                ? "Bác sĩ mô phỏng lấy kết quả LIS cho xét nghiệm \"" + labResult.getTestType() + "\""
                : "Hệ thống LIS gửi kết quả thật cho xét nghiệm \"" + labResult.getTestType() + "\"";

        systemLogService.logActivity("LabResult", labResult.getLabResultId(), action, description);

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

