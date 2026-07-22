package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.UpdateClinicalSymptomsRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionDetailResponse;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResponse;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.repository.DiseaseTypeRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.DiseaseTypeService;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Author: GiangLTHE194888
 * Task: Manages doctor-side diagnosis sessions, including patient symptoms, reviews, session status, and medical images.
 */
@Slf4j
@Controller
@RequestMapping("/doctor/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorDiagnosisController {

    private final DoctorDiagnosisService doctorDiagnosisService;
    private final DiseaseTypeService diseaseTypeService;
    private final com.mycompany.jpademo.backend.service.interfaces.DoctorPrescriptionService doctorPrescriptionService;

    /** Retrieves a paginated list of diagnosis sessions assigned to the current doctor. */
    @GetMapping
    public String getMySessions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DiagnosisSessionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        Integer doctorId = userDetails.getUser().getUserId();
        Page<DoctorSessionResponse> sessions = doctorDiagnosisService.getSessionsByDoctor(
                doctorId, pageable, keyword, status, startDate, endDate);
        model.addAttribute("sessions", sessions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("statuses", DiagnosisSessionStatus.values());

        return "doctor/sessions";
    }

    /** Retrieves detailed information for a specific diagnosis session. */
    @GetMapping("/{sessionId}")
    public String getSessionDetail(
            @PathVariable Integer sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        Integer doctorId = userDetails.getUser().getUserId();

        DoctorSessionDetailResponse sessionDetail = doctorDiagnosisService.getSessionDetail(sessionId, doctorId);
        model.addAttribute("sessionDetail", sessionDetail);
        model.addAttribute("statuses", DiagnosisSessionStatus.values());
        model.addAttribute("diseaseTypes", diseaseTypeService.getAllDiseaseTypes());
        model.addAttribute("activeDrugs", doctorPrescriptionService.getActiveDrugs());
        model.addAttribute("prescription", doctorPrescriptionService.getPrescriptionBySessionId(sessionId).orElse(null));
        return "doctor/session-detail";
    }

    /** Updates the status of a specific diagnosis session. */
    @PostMapping("/{sessionId}/status")
    public String updateSessionStatus(
            @PathVariable Integer sessionId,
            @RequestParam DiagnosisSessionStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            UpdateSessionStatusRequest request = new UpdateSessionStatusRequest();
            request.setSessionId(sessionId);
            request.setStatus(status);

            Integer doctorId = userDetails.getUser().getUserId();
            doctorDiagnosisService.updateSessionStatus(doctorId, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }

    /** Updates the sharing preference/status of a diagnosis session. */
    @PostMapping("/{sessionId}/share")
    public String updateSessionShare(
            @PathVariable Integer sessionId,
            @RequestParam Boolean isShared,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            UpdateSessionShareRequest request = new UpdateSessionShareRequest();
            request.setSessionId(sessionId);
            request.setIsShared(isShared);

            Integer doctorId = userDetails.getUser().getUserId();
            doctorDiagnosisService.updateSessionShare(doctorId, request);
            redirectAttributes.addFlashAttribute("success",
                    isShared ? "Đã chia sẻ phiên chẩn đoán!" : "Đã hủy chia sẻ phiên chẩn đoán!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }

    /** API endpoint to fetch all symptoms associated with a specific diagnosis session. */
    @GetMapping("/{sessionId}/symptoms")
    @ResponseBody
    public List<SymptomResponse> getSessionSymptoms(@PathVariable Integer sessionId,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer doctorId = userDetails.getUser().getUserId();
        return doctorDiagnosisService.getSessionSymptoms(sessionId, doctorId);
    }

    /** Updates/saves clinical symptoms for a diagnosis session. */
    @PostMapping("/{sessionId}/symptoms")
    public String updateSessionSymptoms(
            @PathVariable Integer sessionId,
            @Valid @ModelAttribute UpdateClinicalSymptomsRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Dữ liệu không hợp lệ");
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/doctor/sessions/" + sessionId;
        }
        try {
            Integer doctorId = userDetails.getUser().getUserId();
            doctorDiagnosisService.updateClinicalSymptoms(doctorId, sessionId, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật triệu chứng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/doctor/sessions/" + sessionId;
    }

    /** Sets the symptom input method/mode for a diagnosis session. */
    @PostMapping("/{sessionId}/input-mode")
    public String setClinicalInputMode(
            @PathVariable Integer sessionId,
            @RequestParam com.mycompany.jpademo.backend.enums.ClinicalInputMode clinicalInputMode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Integer doctorId = userDetails.getUser().getUserId();
            doctorDiagnosisService.setClinicalInputMode(doctorId, sessionId, clinicalInputMode);
            redirectAttributes.addFlashAttribute("success", "Đã chọn chế độ nhập triệu chứng: " + clinicalInputMode.name());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }

    /** Saves the doctor's review, conclusions, or diagnosis results for a session. */
    @PostMapping("/{sessionId}/review")
    public String saveSessionReview(
            @PathVariable Integer sessionId,
            @Valid @ModelAttribute com.mycompany.jpademo.backend.dto.request.CreateReviewRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Dữ liệu không hợp lệ");
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/doctor/sessions/" + sessionId;
        }
        try {
            Integer doctorId = userDetails.getUser().getUserId();
            doctorDiagnosisService.saveReview(doctorId, sessionId, request);
            redirectAttributes.addFlashAttribute("success", "Đã lưu kết luận bệnh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/doctor/sessions/" + sessionId;
    }

    /** Issues a new medical imaging order/request for a session. */
    @PostMapping("/{sessionId}/medical-images")
    public String createMedicalImage(
            @PathVariable Integer sessionId,
            @RequestParam String imageType,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Integer doctorId = userDetails.getUser().getUserId();
            doctorDiagnosisService.createMedicalImage(doctorId, sessionId, imageType);
            redirectAttributes.addFlashAttribute("success", "Đã tạo chỉ định hình ảnh y tế thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }

    /** Cancels/deletes a pending medical imaging order from a session. */
    @PostMapping("/{sessionId}/medical-images/retake")
    public String retakeMedicalImage(
            @PathVariable Integer sessionId,
            @RequestParam String originalImageType,
            @RequestParam(required = false) String retakeReason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Integer doctorId = userDetails.getUser().getUserId();
            String newImageType = "[CHỤP LẠI] " + originalImageType;
            if (retakeReason != null && !retakeReason.isBlank()) {
                newImageType += " - Lý do: " + retakeReason.trim();
            }
            doctorDiagnosisService.createMedicalImage(doctorId, sessionId, newImageType);
            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu siêu âm lại đến bác sĩ siêu âm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }

    @PostMapping("/{sessionId}/medical-images/{imageId}/delete")
    public String deleteMedicalImage(
            @PathVariable Integer sessionId,
            @PathVariable Integer imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Integer doctorId = userDetails.getUser().getUserId();
            doctorDiagnosisService.deleteMedicalImage(doctorId, sessionId, imageId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa chỉ định hình ảnh y tế chờ xử lý!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }
}