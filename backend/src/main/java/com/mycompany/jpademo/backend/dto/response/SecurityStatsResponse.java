package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SecurityStatsResponse {
    private Long totalRequestsToday;
    private Long totalBlockedIps;
    private double avgRequestPerMinute;
}
