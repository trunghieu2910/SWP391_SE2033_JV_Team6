package com.mycompany.jpademo.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordFormRequest {
    private String newPassword;
    private String confirmPassword;
}
