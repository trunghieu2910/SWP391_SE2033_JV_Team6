package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.DiagnosisStatisticsResponse;

import java.time.LocalDate;

public interface DiagnosisStatisticsService {

    /**
     * Builds the disease-type breakdown for the given date range.
     * startDate/endDate may be null; sensible defaults are applied
     * internally (from = 2000-01-01, to = today).
     */
    DiagnosisStatisticsResponse getDiseaseStatistics(LocalDate startDate, LocalDate endDate);
}