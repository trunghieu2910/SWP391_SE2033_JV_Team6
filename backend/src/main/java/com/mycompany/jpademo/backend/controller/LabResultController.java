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

/**
 * Web endpoints for a doctor to create, view, and delete lab test
 * orders on a diagnosis session, plus the read-only view for a
 * patient to see their own results.
 */
@Controller
@RequiredArgsConstructor
public class LabResultController {

    private final LabResultService labResultService;
    private final DiagnosisSessionRepository sessionRepository;

    /** Handles form submission for creating a new lab order; redirects back to the session detail page either way. */
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

    /** Patient-facing list view of their own lab results for a session (subject to the session being shared). */
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

    /** Deletes a still-PENDING lab order and redirects back to the session detail page. */
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