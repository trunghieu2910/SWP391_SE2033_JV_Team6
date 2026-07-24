package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.projection.DiseaseStatItem;
import com.mycompany.jpademo.backend.dto.response.DiagnosisStatisticsResponse;
import com.mycompany.jpademo.backend.repository.ReviewRepository;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for the "Diagnosis Statistics" page: resolves the
 * effective date range (applying defaults, validating start <= end),
 * then delegates the actual aggregation to ReviewRepository.
 */
@Service
@RequiredArgsConstructor
public class DiagnosisStatisticsServiceImpl implements DiagnosisStatisticsService {

    private final ReviewRepository reviewRepository;

    @Override
    public DiagnosisStatisticsResponse getDiseaseStatistics(LocalDate startDate, LocalDate endDate) {

        LocalDate from = startDate != null ? startDate : LocalDate.of(2000, 1, 1);
        LocalDate to = endDate != null ? endDate : LocalDate.now();

        List<DiseaseStatItem> stats;
        String dateRangeError = null;

        if (from.isAfter(to)) {
            dateRangeError = "Ngày bắt đầu không được sau ngày kết thúc.";
            stats = List.of();
        } else {
            stats = reviewRepository.countByDiseaseTypeBetween(
                    from.atStartOfDay(),
                    to.atTime(LocalTime.MAX)
            );
        }

        return DiagnosisStatisticsResponse.builder()
                .stats(stats)
                .startDate(from)
                .endDate(to)
                .dateRangeError(dateRangeError)
                .build();
    }
}