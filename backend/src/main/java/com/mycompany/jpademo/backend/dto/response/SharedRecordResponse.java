package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRecordResponse {
    private Integer sessionId;
    private String doctorName; // Name of the doctor who shared it
    private String gender;
    private Integer age;
    private String diseaseType;
    private String aiImageUrl;
    private LocalDateTime createdAt;
}
