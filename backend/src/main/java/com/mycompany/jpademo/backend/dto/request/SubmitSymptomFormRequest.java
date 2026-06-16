package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitSymptomFormRequest {
    @NotNull(message = "Chưa nhập đủ thông tin")
    @Positive(message = "Cân nặng phải là số dương")
    private Double weight;

    @NotNull(message = "Chưa nhập đủ thông tin")
    @Positive(message = "Chiều cao phải là số dương")
    private Double height;

    @NotNull(message = "Chưa nhập đủ thông tin")
    private String menopauseStatus; // NOT_YET or ALREADY

    @NotEmpty(message = "Chưa nhập đủ thông tin")
    private List<Integer> symptoms; // List of symptom IDs

    @NotNull(message = "Chưa nhập đủ thông tin")
    private String symptomDuration; // LESS_THAN_1_MONTH, MONTHS_1_3, MONTHS_3_6, MORE_THAN_6_MONTHS

    @NotNull(message = "Chưa nhập đủ thông tin")
    private Boolean symptomProgressing; // true or false
}
