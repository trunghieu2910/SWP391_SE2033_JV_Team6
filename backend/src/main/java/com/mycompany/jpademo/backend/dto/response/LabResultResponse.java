package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.LabResultStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Response DTO returned to the UI (doctor/patient views) representing
 * a lab test order together with its current status and, once
 * completed, the list of measured parameter values.
 */
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
