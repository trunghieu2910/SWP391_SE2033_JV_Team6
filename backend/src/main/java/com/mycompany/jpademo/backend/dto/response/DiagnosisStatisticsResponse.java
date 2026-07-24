package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.dto.projection.DiseaseStatItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * Wraps everything the "Diagnosis Statistics" page needs to render:
 * the aggregated stats themselves, the resolved date range actually
 * used (after defaults are applied), and an optional validation error.
 */
@Getter
@Builder
public class DiagnosisStatisticsResponse {
    private List<DiseaseStatItem> stats;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dateRangeError;
}