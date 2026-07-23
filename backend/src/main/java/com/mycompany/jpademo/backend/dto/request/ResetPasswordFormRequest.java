package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordFormRequest {

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;
}