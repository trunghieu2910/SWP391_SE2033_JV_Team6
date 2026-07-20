package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "Vui lòng chọn loại bệnh")
    private Integer diseaseTypeId;

    @NotBlank(message = "Kế hoạch điều trị không được để trống")
    private String treatmentPlan;

    private String doctorAdvice;

    private String note;
}
