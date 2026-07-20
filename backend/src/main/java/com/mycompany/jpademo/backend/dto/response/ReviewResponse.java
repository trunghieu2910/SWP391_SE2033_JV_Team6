package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Integer reviewId;
    private Integer sessionId;
    private Integer diseaseTypeId;
    private String finalDiagnosis;
    private String treatmentPlan;
    private String doctorAdvice;
    private String note;
    private LocalDateTime reviewedAt;
}
