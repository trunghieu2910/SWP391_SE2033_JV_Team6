package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateLabResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.entity.LabResultParameter;
import com.mycompany.jpademo.backend.entity.Parameter;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.LabResultParameterRepository;
import com.mycompany.jpademo.backend.repository.LabResultRepository;
import com.mycompany.jpademo.backend.repository.ParameterRepository;
import com.mycompany.jpademo.backend.service.interfaces.LabResultService;
import lombok.RequiredArgsConstructor;
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

        LabResult labResult = LabResult.builder()
                .diagnosisSession(session)
                .testType(request.getTestType())
                .build();

        labResult = labResultRepository.save(labResult);

        List<LabResultParameter> savedParameters = new ArrayList<>();

        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
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

        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy phiên khám với ID: " + sessionId);
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
