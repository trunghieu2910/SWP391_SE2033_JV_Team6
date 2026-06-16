package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class MedicalRecordDetailResponse {
    // Session Info
    private Integer sessionID;
    private Date createdAt;
    private DiagnosisSessionStatus status;
    private Double weight;
    private Double height;
    private Boolean isShared;

    // Patient Info
    private String patientFullName;
    private String patientFirstName;
    private String patientLastName;
    private String patientNationalID;
    private Date patientDob;
    private String patientGender;
    private String patientPhone;
    private String patientAddress;

    // Doctor Info
    private String doctorFullName;

    // Symptom Info
    private Integer symptomResultID;
    private String symptomResultStatus;
    private String symptomName;
    private String symptomDescription;
    private List<SymptomDTO> symptoms;
    private String menopauseStatus;
    private String symptomDuration;
    private Boolean symptomProgressing;

    // Medical Info
    private String bloodPressure;
    private Integer heartRate;
    private Double temperature;
    private String nationalID;
    private String gender;
    private Integer age;

    // Lab & Image
    private List<LabTestDTO> labTests;
    private List<ImageDTO> medicalImages;

    // Review Info
    private Integer reviewID;
    private String finalDiagnosis;
    private String icd10Code;
    private String verdict;
    private String treatmentPlan;
    private String doctorAdvice;
    private String note;
    private Date reviewedAt;
    private String reviewedByDoctorName;



    @Data
    public static class SymptomDTO {
        private Integer symptomID;
        private String symptomName;
    }

    @Data
    public static class LabTestDTO {
        private Integer labResultID;
        private String testName;
        private Date testedAt;
        private String status;
        private List<ParamDTO> parameters;
    }

    @Data
    public static class ParamDTO {
        private Integer labResultParameterID;
        private String paramName;
        private String value;
        private String unit;
    }

    @Data
    public static class ImageDTO {
        private Integer medicalImageID;
        private String imageType;
        private Date createdAt;
        private String status;
        private List<ImageDetailDTO> details;
    }

    @Data
    public static class ImageDetailDTO {
        private Integer imageID;
        private String imageUrl;
        private Date uploadedAt;
    }
}