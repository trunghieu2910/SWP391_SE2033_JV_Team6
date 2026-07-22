package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DiagnosisSessionResponse {
    private Integer sessionId;
    private Integer patientId;
    private String patientName;
    private String patientCccd;
    private Double weight;
    private Double height;
    private DiagnosisSessionStatus status;
    private SymptomResultStatus symptomResultStatus;
    private ClinicalInputMode clinicalInputMode;
    private LocalDateTime createdAt;
    private SymptomResultResponse symptomResult;
    private Integer medicalImageDetailsId;
    private String imageType;
}
