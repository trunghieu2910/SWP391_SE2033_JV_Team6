package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private Integer sessionID;
    private Date createdAt;
    private DiagnosisSessionStatus status;
    private String finalDiagnosis;
}