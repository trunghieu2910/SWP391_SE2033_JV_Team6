package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.DiagnosisStatisticsResponse;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

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

    private final DiagnosisStatisticsService diagnosisStatisticsService;

    /**
     * Renders the statistics page. startDate/endDate are optional query
     * parameters (?startDate=...&endDate=...); defaulting and validation
     * are handled entirely by DiagnosisStatisticsService — this method
     * only wires the HTTP request/response to the service and view.
     */
    @GetMapping
    public String showStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        DiagnosisStatisticsResponse result = diagnosisStatisticsService.getDiseaseStatistics(startDate, endDate);

        model.addAttribute("stats", result.getStats());
        model.addAttribute("startDate", result.getStartDate());
        model.addAttribute("endDate", result.getEndDate());
        model.addAttribute("dateRangeError", result.getDateRangeError());
        return "doctor/statistics";
    }
}