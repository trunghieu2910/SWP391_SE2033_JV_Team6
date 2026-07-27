package com.mycompany.jpademo.backend.dto.request;

import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDiagnosisSessionRequest {
    @NotNull(message = "Chưa nhập đủ thông tin")
    private Integer patientId;

    @NotNull(message = "Vui lòng chọn bác sĩ")
    private Integer doctorId;

    @NotNull(message = "Vui lòng chọn người nhập triệu chứng")
    @Builder.Default
    private ClinicalInputMode clinicalInputMode = ClinicalInputMode.PATIENT;
}
