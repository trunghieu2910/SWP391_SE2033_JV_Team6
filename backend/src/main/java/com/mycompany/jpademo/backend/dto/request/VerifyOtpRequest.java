package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

    @NotBlank(message = "Email không được để trống!")
    private String email;

    @NotBlank(message = "OTP không được để trống!")
    private String otp;
}
