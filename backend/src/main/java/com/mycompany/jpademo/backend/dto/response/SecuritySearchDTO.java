package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SecuritySearchDTO {
    private String ipAddress;
    private String reason;
    private String blockedAt;
    private String createdBy;
}