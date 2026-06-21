package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateLabResultRequest {

    @NotNull(message = "Mã phiên khám không được để trống")
    @Positive(message = "Mã phiên khám phải là số dương")
    private Integer sessionId;

    @NotBlank(message = "Loại xét nghiệm không được để trống")
    private String testType;
}
