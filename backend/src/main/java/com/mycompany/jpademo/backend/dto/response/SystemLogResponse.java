package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogResponse {
    private String userName;

    private Integer logId;

    private String action;

    private String actionDisplay;

    private String description;

    private String targetType;

    private Integer targetId;

    private LocalDateTime performedAt;
}