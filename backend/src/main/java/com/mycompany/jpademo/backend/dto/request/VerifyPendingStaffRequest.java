package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPendingStaffRequest {
    @NotBlank(message = "Mã yêu cầu không được để trống")
    private String requestId;

    @NotBlank
    @Length(min = 6, max = 6)
    private String otp;
}