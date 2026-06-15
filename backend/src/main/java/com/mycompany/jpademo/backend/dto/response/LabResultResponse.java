package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.LabResultStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LabResultResponse {

    private Integer labResultId;

    private Integer sessionId;

    private String testType;

    private LabResultStatus status;

    private LocalDateTime createdAt;

    private List<ParameterValueResponse> parameters;

    @Data
    @Builder
    public static class ParameterValueResponse {

        private Integer labResultParameterId;

        private Integer parameterId;

        private String parameterName;

        private String unit;

        private String value;
    }
}
