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

import java.util.stream.Collectors;

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

        // LOG chi tiết lỗi validation
        if (bindingResult.hasErrors()) {
            log.error("Validation errors when saving prescription for session {}:", sessionId);
            String errorDetails = bindingResult.getAllErrors().stream()
                    .map(error -> {
                        if (error instanceof org.springframework.validation.FieldError) {
                            org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) error;
                            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                        }
                        return error.getDefaultMessage();
                    })
                    .collect(Collectors.joining("; "));

            log.error("Errors: {}", errorDetails);

            // Lưu lỗi để hiển thị trên view
            redirectAttributes.addFlashAttribute("error", "Lỗi validation: " + errorDetails);
            redirectAttributes.addFlashAttribute("validationErrors", bindingResult.getAllErrors());

            // Giữ lại dữ liệu đã nhập để user không phải nhập lại
            redirectAttributes.addFlashAttribute("prescriptionFormData", request);

            return "redirect:/doctor/sessions/" + sessionId + "?openPrescription=true&error=true";
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
