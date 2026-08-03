package com.mycompany.jpademo.backend.dto.request;

import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateDiagnosisSessionRequest {
    @NotNull(message = "Chưa nhập đủ thông tin")
    private Integer patientId;

    @NotNull(message = "Vui lòng chọn bác sĩ")
    private Integer doctorId;

    private ClinicalInputMode clinicalInputMode;
}
