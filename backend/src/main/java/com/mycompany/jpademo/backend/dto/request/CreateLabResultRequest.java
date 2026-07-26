package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request payload submitted by a doctor to order a new lab test
 * (indication) for a diagnosis session. Only the test type is chosen
 * by the doctor at creation time — parameter values are filled in
 * later, either by the LIS webhook or the "simulate" button.
 */
@Data
public class CreateLabResultRequest {

    @NotNull(message = "Mã ca khám không được để trống")
    @Positive(message = "Mã ca khám phải là số dương")
    private Integer sessionId;

    @NotBlank(message = "Loại xét nghiệm không được để trống")
    private String testType;
}
