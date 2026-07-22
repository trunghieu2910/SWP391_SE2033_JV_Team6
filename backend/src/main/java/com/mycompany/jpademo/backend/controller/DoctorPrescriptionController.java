package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreateDoctorPrescriptionRequest;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.DoctorPrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/doctor/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorPrescriptionController {

    private final DoctorPrescriptionService doctorPrescriptionService;

    @PostMapping("/{sessionId}/prescription")
    public String savePrescription(
            @PathVariable Integer sessionId,
            @Valid @ModelAttribute CreateDoctorPrescriptionRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        request.setSessionId(sessionId);

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Dữ liệu đơn thuốc không hợp lệ.");
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/doctor/sessions/" + sessionId + "?openPrescription=true";
        }

        try {
            Integer doctorId = userDetails.getUser().getUserId();
            doctorPrescriptionService.savePrescription(doctorId, request);
            redirectAttributes.addFlashAttribute("success", "Kê đơn thuốc thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi kê đơn thuốc cho ca chẩn đoán #{}: {}", sessionId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/doctor/sessions/" + sessionId + "?openPrescription=true";
    }
}
