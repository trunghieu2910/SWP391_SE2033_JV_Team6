package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockIpRequest {
    @NotBlank(message = "Địa chỉ IP không được để trống")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Địa chỉ IP không hợp lệ")
    private String ipAddress;

    @NotBlank(message = "Lý do chặn không được để trống")
    private String reason;
}
