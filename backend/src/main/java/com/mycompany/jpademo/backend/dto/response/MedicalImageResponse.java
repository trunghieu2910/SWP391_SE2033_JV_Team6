package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.MedicalImageStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalImageResponse {
    private Integer medicalImageId;

    private Integer sessionId;

    private String imageType;

    private MedicalImageStatus status;

    private LocalDateTime createdAt;

    List<MedicalImageDetailResponse> images;
}
