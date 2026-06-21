package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiateCreateDoctorResponse {
    private String requestId;
    private String message;
}