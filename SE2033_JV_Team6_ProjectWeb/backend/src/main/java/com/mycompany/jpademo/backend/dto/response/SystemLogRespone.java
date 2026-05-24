package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SystemLogRespone {
    private Long logID;

    private String action;

    private String description;

    private String targetType;

    private Long targetId;

    private String ipAddress;

    private String performedBy;

    private LocalDateTime performedAt;
}
