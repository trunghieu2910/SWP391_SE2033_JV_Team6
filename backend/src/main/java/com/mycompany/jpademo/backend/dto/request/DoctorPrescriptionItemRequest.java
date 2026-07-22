package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorPrescriptionItemRequest {

    @NotNull(message = "Vui lòng chọn thuốc")
    private Integer drugId;

    @NotNull(message = "Số lượng thuốc không được để trống")
    @Min(value = 1, message = "Số lượng thuốc phải lớn hơn 0")
    private Integer quantityPrescribed;

    @NotBlank(message = "Cách sử dụng thuốc không được để trống")
    private String instruction;

    private BigDecimal dosePerTime;

    private Integer timesPerDay;

    private Integer daysOfTreatment;
}
