package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPendingDoctorRequest {
    @NotBlank
    private String otp;
}