package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {
    private Long totalUsers;

    private Long totalDoctors;

    private Long totalPatients;

    private Long blockedUsers;

    private Long totalDiagnosisSessions;
}
