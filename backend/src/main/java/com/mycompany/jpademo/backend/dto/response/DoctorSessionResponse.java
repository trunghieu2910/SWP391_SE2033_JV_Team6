package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSessionResponse {
    private Integer sessionId;

    private Integer patientId;

    private String fullName;

    private DiagnosisSessionStatus status;

    private Boolean isShared;

    private LocalDateTime createdAt;
}
