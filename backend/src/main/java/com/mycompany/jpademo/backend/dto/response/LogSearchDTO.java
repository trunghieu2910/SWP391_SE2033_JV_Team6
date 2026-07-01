package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LogSearchDTO {
    private Integer logId;
    private String action;
    private String actionDisplay;
    private String description;
    private String username;
    private LocalDateTime performedAt;
}