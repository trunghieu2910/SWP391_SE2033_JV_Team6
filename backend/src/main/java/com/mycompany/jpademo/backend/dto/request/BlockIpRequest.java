package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockIpRequest {
    @NotBlank(message = "IP address cannot be empty")
    private String ipAddress;
    private String reason;
}
