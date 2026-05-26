package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class VerifyOtpResponse {

    private String resetToken;
}
