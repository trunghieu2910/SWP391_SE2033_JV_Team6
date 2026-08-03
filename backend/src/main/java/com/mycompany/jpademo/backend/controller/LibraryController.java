package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.SharedRecordResponse;
import com.mycompany.jpademo.backend.service.interfaces.DiseaseTypeService;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/doctor/library")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class LibraryController {

    private final DoctorDiagnosisService doctorDiagnosisService;
    private final DiseaseTypeService diseaseTypeService;

    @GetMapping
    public String getSharedRecords(
            @RequestParam(required = false, defaultValue = "") String diseaseType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<SharedRecordResponse> sharedRecordsPage = doctorDiagnosisService.getSharedRecords(diseaseType, pageable);
        
        model.addAttribute("sharedRecords", sharedRecordsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sharedRecordsPage.getTotalPages());
        model.addAttribute("currentTab", "shared-records");
        model.addAttribute("title", "Bảng Hội Chẩn");
        model.addAttribute("diseaseTypes", diseaseTypeService.getAllDiseaseTypes());
        model.addAttribute("selectedDiseaseType", diseaseType);
        return "doctor/shared-records";
    }

    @GetMapping("/{id}")
    public String getSharedRecordDetail(@org.springframework.web.bind.annotation.PathVariable("id") Integer id, Model model) {
        com.mycompany.jpademo.backend.dto.response.DoctorSessionDetailResponse sessionDetail = doctorDiagnosisService.getSharedSessionDetail(id);
        model.addAttribute("sessionDetail", sessionDetail);
        model.addAttribute("currentTab", "shared-records");
        model.addAttribute("title", "Chi Tiết Bảng Hội Chẩn");
        return "doctor/shared-record-detail";
    }
}
