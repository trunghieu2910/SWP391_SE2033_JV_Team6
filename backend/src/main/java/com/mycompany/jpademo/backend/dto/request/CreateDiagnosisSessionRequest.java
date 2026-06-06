package com.mycompany.jpademo.backend.dto.request;

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
public class CreateDiagnosisSessionRequest {
    @NotNull(message = "Chưa nhập đủ thông tin")
    private Integer patientId;

    @Positive(message = "Cân nặng phải là số dương")
    private Double weight;

    @Positive(message = "Chiều cao phải là số dương")
    private Double height;
}
