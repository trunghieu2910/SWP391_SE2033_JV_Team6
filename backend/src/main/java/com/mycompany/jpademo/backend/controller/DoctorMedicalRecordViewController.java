package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SOLID:
 *  - SRP: Controller này chỉ phụ trách render Thymeleaf views cho trang hồ sơ bệnh án của bác sĩ.
 *         Toàn bộ logic nghiệp vụ nằm trong MedicalRecordService.
 *  - OCP: Không sửa MedicalRecordController (REST API cũ), chỉ mở rộng thêm controller view mới.
 *  - LSP: Không vi phạm — không kế thừa class nào.
 *  - ISP: Phụ thuộc đúng interface MedicalRecordService, không phụ thuộc interface dư thừa.
 *  - DIP: Phụ thuộc vào interface MedicalRecordService, không phụ thuộc implementation cụ thể.
 */
@Controller
@RequestMapping("/doctor/medical-records")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorMedicalRecordViewController {

    private final MedicalRecordService medicalRecordService;

    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Trang danh sách hồ sơ bệnh án — maps to: GET /doctor/medical-records
     *
     * @param keyword   từ khóa tìm kiếm (tên bệnh nhân hoặc CCCD)
     * @param status    lọc theo trạng thái (COMPLETED, PENDING, hoặc rỗng = tất cả)
     * @param page      trang hiện tại (0-based)
     * @param model     Thymeleaf model
     * @return          tên template "doctor/medical-records/list"
     */
    @GetMapping
    public String listMedicalRecords(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        String keywordParam = keyword.isBlank() ? null : keyword.trim();
        String statusParam  = status.isBlank()  ? null : status.trim();

        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by("createdAt").descending());
        Page<MedicalRecordResponse> recordPage =
                medicalRecordService.getMedicalRecords(keywordParam, statusParam, null, pageable);

        long completedCount = recordPage.getContent().stream()
                .filter(r -> r.getStatus() != null && "COMPLETED".equals(r.getStatus().name()))
                .count();
        long pendingCount = recordPage.getContent().stream()
                .filter(r -> r.getStatus() != null && "PENDING".equals(r.getStatus().name()))
                .count();
        long withDiagnosisCount = recordPage.getContent().stream()
                .filter(r -> r.getDiagnosis() != null && !r.getDiagnosis().isEmpty()
                          && !"Chưa có chẩn đoán".equals(r.getDiagnosis()))
                .count();

        model.addAttribute("records",        recordPage.getContent());
        model.addAttribute("currentPage",    page);
        model.addAttribute("totalPages",     recordPage.getTotalPages());
        model.addAttribute("totalElements",  recordPage.getTotalElements());
        model.addAttribute("keyword",        keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("completedCount",     completedCount);
        model.addAttribute("pendingCount",       pendingCount);
        model.addAttribute("withDiagnosisCount", withDiagnosisCount);
        
        if (userDetails != null && userDetails.getUser() != null) {
            model.addAttribute("doctorName", userDetails.getUser().getFullName());
        } else {
            model.addAttribute("doctorName", "Bác sĩ");
        }

        return "doctor/medical-records-list-doctor";
    }

    /**
     * Trang chi tiết hồ sơ bệnh án — maps to: GET /doctor/medical-records/{sessionId}
     *
     * @param sessionId mã phiên khám
     * @param model     Thymeleaf model
     * @return          tên template "doctor/medical-records/detail"
     */
    @GetMapping("/{sessionId}")
    public String medicalRecordDetail(
            @PathVariable Integer sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        // isPatient = false → bác sĩ thấy toàn bộ thông tin, không bị data masking
        MedicalRecordDetailResponse detail =
                medicalRecordService.getMedicalRecordDetail(sessionId, false);

        model.addAttribute("record", detail);
        
        if (userDetails != null && userDetails.getUser() != null) {
            model.addAttribute("doctorName", userDetails.getUser().getFullName());
        } else {
            model.addAttribute("doctorName", "Bác sĩ");
        }

        return "doctor/medical-records-detail-doctor";
    }
}
