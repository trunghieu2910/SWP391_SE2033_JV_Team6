package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorPrescriptionRequest {

    @NotNull(message = "Mã ca chẩn đoán không được để trống")
    private Integer sessionId;

    @NotEmpty(message = "Đơn thuốc phải có ít nhất 1 loại thuốc")
    @Valid
    private List<DoctorPrescriptionItemRequest> items;

    private String notes;
}
