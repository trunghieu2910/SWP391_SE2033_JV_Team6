package com.mycompany.jpademo.backend.dto.response;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class MedicalRecordDetailResponse {
    private Integer sessionID;
    private Date createdAt;
    private String status;
    private String patientFirstName;
    private String patientLastName;
    private Date patientDob;
    private String patientGender;
    private String patientPhone;
    private String symptomName;
    private String symptomDescription;
    private Double weight;
    private Double height;
    private String bloodPressure;
    private Integer heartRate;
    private Double temperature;

    private List<LabTestDTO> labTests;
    private List<ImageDTO> medicalImages;

    private String verdict;
    private String finalDiagnosis;
    private String icd10Code;
    private String treatmentPlan;
    private String doctorAdvice;
    private String note;



    @Data
    public static class LabTestDTO {
        private String testName;
        private Date testedAt;
        private List<ParamDTO> parameters;
    }

    @Data
    public static class ParamDTO {
        private String paramName;
        private String value;
        private String unit;
    }

    @Data
    public static class ImageDTO {
        private String imageType;
        private Date createdAt;
        private List<String> imageUrls;
    }
}