package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalImageDetailResponse {
    private Integer imageId;

    private String imageUrl;

    private String aiImageUrl;

    private Double confidenceScore;

    private String technicalConclusion;

    private String imgResultConclusion;

    private LocalDateTime uploadedAt;

}
