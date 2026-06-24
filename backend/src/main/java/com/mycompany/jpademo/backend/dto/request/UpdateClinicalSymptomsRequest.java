package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClinicalSymptomsRequest {
    @NotNull(message = "Chưa nhập đủ thông tin")
    @Positive(message = "Chiều cao phải là số dương")
    private Double height;

    @NotNull(message = "Chưa nhập đủ thông tin")
    @Positive(message = "Cân nặng phải là số dương")
    private Double weight;
    private String menopauseStatus;
    private List<Integer> abnormalBleedingIds;
    private List<Integer> abnormalDischargeIds;
    private List<Integer> painIds;
    private List<Integer> urinarySymptomsIds;
    private List<Integer> digestiveSymptomsIds;
    private Map<String, Boolean> systemicSymptoms;
    private Map<String, Boolean> riskFactors;
    private String symptomDuration;
    private Boolean symptomProgressing;
}