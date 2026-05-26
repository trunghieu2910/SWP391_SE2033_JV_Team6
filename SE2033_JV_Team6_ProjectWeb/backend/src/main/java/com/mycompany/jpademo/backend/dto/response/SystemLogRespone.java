package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SystemLogRespone {
    private Integer logID;

    private String action;

    private String description;

    private String targetType;

    private Integer targetId;

    private LocalDateTime performedAt;
}
