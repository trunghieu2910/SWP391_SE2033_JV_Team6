package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordResponse {
    private Integer id;
    private String patientName;
    private String diagnosis;
    private Date visitDate;
    private String symptoms;
    private String prescription;
    private String doctorNotes;
    private Boolean isShared;
    private String nationalID;
    private String gender;
    private Integer age;


    // Chi tiết (detail view)
    private Integer sessionID;
    private Date createdAt;
    private DiagnosisSessionStatus status;
    private String finalDiagnosis;

}