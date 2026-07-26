package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ChartStatsResponse {
    private List<MonthlyStats> userRegistrations;
    private List<MonthlyStats> requestTrends;
}