package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreateLabResultRequest;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.service.interfaces.LabResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LabResultController {

    private final LabResultService labResultService;
    private final DiagnosisSessionRepository sessionRepository;

    // ══════════════════════════════════════════════
    //  TẠO XÉT NGHIỆM (đã làm ở phần trước — giữ nguyên)
    // ══════════════════════════════════════════════

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/lab-results/create")
    public String showCreateForm(@RequestParam("sessionId") Integer sessionId, Model model) {
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiên khám với ID: " + sessionId));

        CreateLabResultRequest form = new CreateLabResultRequest();
        form.setSessionId(sessionId);

        model.addAttribute("labResultForm", form);
        model.addAttribute("session", session);
        return "doctor/lab-result";
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/lab-results/create")
    public String createLabResult(
            @Valid @ModelAttribute("labResultForm") CreateLabResultRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("session",
                    sessionRepository.findById(form.getSessionId()).orElse(null));
            return "doctor/lab-result";
        }

        try {
            labResultService.createLabResult(form);
            redirectAttributes.addFlashAttribute("success",
                    "Tạo chỉ định xét nghiệm thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/doctor/sessions/" + form.getSessionId() + "?openLab=true";
    }

    // ══════════════════════════════════════════════
    //  XEM XÉT NGHIỆM (chức năng mới của phần này)
    // ══════════════════════════════════════════════

    // ---- Bác sĩ xem ----
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/lab-results/session/{sessionId}")
    public String viewByDoctorSession(
            @PathVariable Integer sessionId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            List<LabResultResponse> results = labResultService.getLabResultsBySession(sessionId);
            DiagnosisSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy phiên khám với ID: " + sessionId));

            model.addAttribute("labResults", results);
            model.addAttribute("session", session);
            model.addAttribute("sessionId", sessionId);
            return "doctor/lab-result-list";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/doctor/patients";
        } catch (UnauthorizedActionException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/doctor/patients";
        }
    }

    // ---- Bệnh nhân xem ----
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/lab-results/session/{sessionId}")
    public String viewByPatientSession(
            @PathVariable Integer sessionId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            List<LabResultResponse> results = labResultService.getLabResultsBySession(sessionId);
            DiagnosisSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy phiên khám với ID: " + sessionId));

            model.addAttribute("labResults", results);
            model.addAttribute("session", session);
            model.addAttribute("sessionId", sessionId);
            return "patient/lab-result-list";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/patient/dashboard";
        } catch (UnauthorizedActionException ex) {
            // Bao gồm cả trường hợp session.isShared = false — service đã tự chặn
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/patient/dashboard";
        }
    }

    // ══════════════════════════════════════════════
    //  XÓA XÉT NGHIỆM (chỉ khi đang PENDING)
    // ══════════════════════════════════════════════

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/lab-results/{labResultId}/delete")
    public String deleteLabResult(
            @PathVariable Integer labResultId,
            @RequestParam("sessionId") Integer sessionId,
            RedirectAttributes redirectAttributes) {

        try {
            labResultService.deleteLabResult(labResultId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa xét nghiệm thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/doctor/sessions/" + sessionId + "?openLab=true";
    }
}