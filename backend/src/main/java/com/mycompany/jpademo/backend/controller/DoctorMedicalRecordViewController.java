package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import com.mycompany.jpademo.backend.service.interfaces.PdfService;
import com.mycompany.jpademo.backend.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/doctor/medical-records")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorMedicalRecordViewController {

    private final MedicalRecordService medicalRecordService;
    private final PdfService pdfService;
    private final PrescriptionRepository prescriptionRepository;
    private final DiagnosisSessionRepository diagnosisSessionRepository;

    private static final int DEFAULT_PAGE_SIZE = 9;


    @GetMapping
    public String listMedicalRecords(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String diseaseType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        String keywordParam = keyword.isBlank() ? null : keyword.trim();
        String statusParam = status.isBlank() ? null : status.trim();
        String diseaseTypeParam = diseaseType.isBlank() ? null : diseaseType.trim();


        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by("createdAt").descending());
        Page<MedicalRecordResponse> recordPage = medicalRecordService.getMedicalRecords(
                keywordParam, statusParam, null, diseaseTypeParam, startDateTime, endDateTime, pageable);


        long completedCount = medicalRecordService.countByStatus(
                keywordParam, "COMPLETED", diseaseTypeParam, startDateTime, endDateTime);
        long pendingCount = medicalRecordService.countByStatus(
                keywordParam, "PENDING", diseaseTypeParam, startDateTime, endDateTime);
        long withDiagnosisCount = medicalRecordService.countWithDiagnosis(
                keywordParam, diseaseTypeParam, startDateTime, endDateTime);

        model.addAttribute("records", recordPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", recordPage.getTotalPages());
        model.addAttribute("totalElements", recordPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("withDiagnosisCount", withDiagnosisCount);
        model.addAttribute("selectedDiseaseType", diseaseType);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);

        if (userDetails != null && userDetails.getUser() != null) {
            model.addAttribute("doctorName", userDetails.getUser().getFullName());
        } else {
            model.addAttribute("doctorName", "Bác sĩ");
        }

        return "doctor/medical-records-list-doctor";
    }

    /**
     * Trang chi tiết hồ sơ bệnh án — maps to: GET
     * /doctor/medical-records/{sessionId}
     *
     * @param sessionId mã ca khám
     * @param model Thymeleaf model
     * @return tên template "doctor/medical-records/detail"
     */
    @GetMapping("/{sessionId}")
    public String medicalRecordDetail(
            @PathVariable Integer sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        // Lấy ca khám từ database
        DiagnosisSession session = diagnosisSessionRepository.findById(sessionId)
                .orElse(null);

        // Kiểm tra bảo mật: Nếu không tìm thấy hoặc Bác sĩ đang đăng nhập KHÔNG PHẢI bác sĩ của ca khám này
        if (session == null || !session.getUser().getUserId().equals(userDetails.getUser().getUserId())) {
            return "redirect:/doctor/medical-records?error=access-denied";
        }

        
        MedicalRecordDetailResponse detail
                = medicalRecordService.getMedicalRecordDetail(sessionId, false);

        model.addAttribute("record", detail);
        model.addAttribute("prescription", prescriptionRepository.findBySessionSessionId(sessionId).orElse(null));

        if (userDetails != null && userDetails.getUser() != null) {
            model.addAttribute("doctorName", userDetails.getUser().getFullName());
        } else {
            model.addAttribute("doctorName", "Bác sĩ");
        }

        return "doctor/medical-records-detail-doctor";
    }
// danh cho xuat file pdf

    @GetMapping("/{sessionId}/export")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> exportMedicalRecordPdf(
            @PathVariable Integer sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Lấy ca khám từ database
        DiagnosisSession session = diagnosisSessionRepository.findById(sessionId)
                .orElse(null);

        // Kiểm tra bảo mật: Chặn nếu không phải bác sĩ phụ trách
        if (session == null || !session.getUser().getUserId().equals(userDetails.getUser().getUserId())) {
            return ResponseEntity.status(403).body(null);
        }

        MedicalRecordDetailResponse record = medicalRecordService.getMedicalRecordDetail(sessionId, false);
        byte[] pdfBytes = pdfService.generateMedicalRecordPdf(record, false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "medical_record_S" + sessionId + "_full.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
