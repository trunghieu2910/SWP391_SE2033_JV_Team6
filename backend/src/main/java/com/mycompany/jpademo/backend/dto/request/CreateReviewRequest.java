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

    @NotBlank(message = "Vui lòng chọn loại bệnh")
    private String diseaseTypeSelection; // giá trị là ID (dạng chuỗi số) hoặc "NEW"

    private String newDiseaseTypeName; // chỉ dùng khi diseaseTypeSelection == "NEW"

    @NotBlank(message = "Kế hoạch điều trị không được để trống")
    private String treatmentPlan;

    private String doctorAdvice;

    private String note;
}
