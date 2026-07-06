package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.UpdateClinicalSymptomsRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionDetailResponse;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResponse;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/doctor/sessions")
@RequiredArgsConstructor
public class DoctorDiagnosisController {

    private final DoctorDiagnosisService doctorDiagnosisService;

    private static final Integer TEST_DOCTOR_ID = 3;

    @GetMapping
    public String getMySessions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DiagnosisSessionStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Integer doctorId = TEST_DOCTOR_ID;
        Page<DoctorSessionResponse> sessions = doctorDiagnosisService.getSessionsByDoctor(
                doctorId, pageable, keyword, status);
        model.addAttribute("sessions", sessions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", DiagnosisSessionStatus.values());

        return "doctor/sessions";
    }

    @GetMapping("/{sessionId}")
    public String getSessionDetail(
            @PathVariable Integer sessionId,
            Model model) {
        Integer doctorId = TEST_DOCTOR_ID;

        DoctorSessionDetailResponse sessionDetail = doctorDiagnosisService.getSessionDetail(sessionId, doctorId);
        model.addAttribute("sessionDetail", sessionDetail);
        model.addAttribute("statuses", DiagnosisSessionStatus.values());

        return "doctor/session-detail";
    }

    @PostMapping("/{sessionId}/status")
    public String updateSessionStatus(
            @PathVariable Integer sessionId,
            @RequestParam DiagnosisSessionStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            UpdateSessionStatusRequest request = new UpdateSessionStatusRequest();
            request.setSessionId(sessionId);
            request.setStatus(status);
            Integer doctorId = TEST_DOCTOR_ID;
            doctorDiagnosisService.updateSessionStatus(doctorId, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }

    @PostMapping("/{sessionId}/share")
    public String updateSessionShare(
            @PathVariable Integer sessionId,
            @RequestParam Boolean isShared,
            RedirectAttributes redirectAttributes) {
        try {
            UpdateSessionShareRequest request = new UpdateSessionShareRequest();
            request.setSessionId(sessionId);
            request.setIsShared(isShared);
            Integer doctorId = TEST_DOCTOR_ID;
            doctorDiagnosisService.updateSessionShare(doctorId, request);
            redirectAttributes.addFlashAttribute("success",
                    isShared ? "Đã chia sẻ phiên chẩn đoán!" : "Đã hủy chia sẻ phiên chẩn đoán!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/sessions/" + sessionId;
    }

    @GetMapping("/{sessionId}/symptoms")
    @ResponseBody
    public List<SymptomResponse> getSessionSymptoms(@PathVariable Integer sessionId) {
        return doctorDiagnosisService.getSessionSymptoms(sessionId);
    }

    @PostMapping("/{sessionId}/symptoms")
    public String updateSessionSymptoms(
            @PathVariable Integer sessionId,
            @Valid @ModelAttribute UpdateClinicalSymptomsRequest request,
            BindingResult bindingResult,
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
            Integer doctorId = TEST_DOCTOR_ID;
            doctorDiagnosisService.updateClinicalSymptoms(doctorId, sessionId, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật triệu chứng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/doctor/sessions/" + sessionId;
    }

    @PostMapping("/{sessionId}/review")
    public String saveSessionReview(
            @PathVariable Integer sessionId,
            @Valid @ModelAttribute com.mycompany.jpademo.backend.dto.request.CreateReviewRequest request,
            BindingResult bindingResult,
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
            Integer doctorId = TEST_DOCTOR_ID;
            doctorDiagnosisService.saveReview(doctorId, sessionId, request);
            redirectAttributes.addFlashAttribute("success", "Đã lưu kết luận bệnh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/doctor/sessions/" + sessionId;
    }
}