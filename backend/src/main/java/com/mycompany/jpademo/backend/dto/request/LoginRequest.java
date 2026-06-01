package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Tài khoản không được để trống!")
    private String login;

    @NotBlank(message = "Mật khẩu không được để trống!")
    private String password;
}

