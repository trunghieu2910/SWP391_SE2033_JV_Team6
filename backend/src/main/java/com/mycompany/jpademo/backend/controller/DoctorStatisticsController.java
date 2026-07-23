package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.projection.DiseaseStatItem;
import com.mycompany.jpademo.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Displays the "Diagnosis Statistics" page for doctors: a breakdown of how
 * many diagnosis reviews were recorded per disease type within a chosen
 * date range. Restricted to the DOCTOR role only.
 */
@Controller
@RequestMapping("/doctor/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorStatisticsController {

    private final ReviewRepository reviewRepository;

    /**
     * Renders the statistics page.
     * startDate/endDate are optional query parameters (?startDate=...&endDate=...);
     * if either is missing, defaults are applied so the page always shows a
     * sensible result on first load:
     *   - from: 2000-01-01 (effectively "since the beginning of the data")
     *   - to:   today
     * The date range is converted to LocalDateTime bounds (start of day /
     * end of day) before being passed to the repository, since Review
     * timestamps are stored with time-of-day precision.
     */
    @GetMapping
    public String showStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

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

        model.addAttribute("stats", stats);
        model.addAttribute("startDate", from);
        model.addAttribute("endDate", to);
        model.addAttribute("dateRangeError", dateRangeError);
        return "doctor/statistics";
    }
}