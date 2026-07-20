package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPageResponse {
    private DashboardStatsResponse stats;
    private ChartStatsResponse charts;
    private List<SystemLogResponse> recentLogs;
    private LocalDate startDate;
    private LocalDate endDate;
    private String errorMessage;
}