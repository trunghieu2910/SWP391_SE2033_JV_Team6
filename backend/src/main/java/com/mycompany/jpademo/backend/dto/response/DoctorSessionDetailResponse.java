package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSessionDetailResponse {
    private Integer sessionId;

    private String status;

    private Boolean isShared;

    private LocalDateTime createdAt;

    private Double height;

    private Double weight;

    private Integer doctorId;

    private String doctorName;

    private ClinicalInputMode clinicalInputMode;

    private Integer patientId;

    private String patientName;

    private String patientGender;

    private LocalDateTime patientDob;

    private String patientAddress;

    private SymptomResultResponse symptomResult;

    private List<LabResultResponse> labResults;

    private List<MedicalImageResponse> medicalImages;

    private ReviewResponse review;
}
