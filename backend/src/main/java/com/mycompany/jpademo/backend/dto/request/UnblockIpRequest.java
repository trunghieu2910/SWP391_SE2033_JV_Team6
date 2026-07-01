package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UnblockIpRequest {
    @NotBlank(message = "Địa chỉ IP không được để trống")
    private String ipAddress;
}
