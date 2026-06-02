package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Token không được để trống!")
    private String resetToken;

    @NotBlank(message = "Mật khẩu mới không được để trống!")
    private String newPassword;
}
