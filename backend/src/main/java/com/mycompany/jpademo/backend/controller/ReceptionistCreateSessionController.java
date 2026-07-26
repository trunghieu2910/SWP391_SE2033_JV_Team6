package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.DoctorWorkloadDto;
import com.mycompany.jpademo.backend.dto.response.PatientSearchResponse;
import com.mycompany.jpademo.backend.dto.response.UserResponse;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import com.mycompany.jpademo.backend.service.interfaces.PatientSearchService;
import com.mycompany.jpademo.backend.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/receptionist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionistCreateSessionController {

    private final PatientSearchService patientSearchService;
    private final UserService userService;
    private final DiagnosisSessionService diagnosisSessionService;
    private final DiagnosisSessionRepository diagnosisSessionRepository;

    private List<DoctorWorkloadDto> getDoctorsWithWorkload() {
        return userService.getActiveDoctors().stream()
                .map(doctor -> {
                    long pendingCount = diagnosisSessionRepository.countByUserUserIdAndStatusNotIn(
                            doctor.getUserId(), java.util.List.of(DiagnosisSessionStatus.COMPLETED, DiagnosisSessionStatus.FAILED));
                    return new DoctorWorkloadDto(doctor, pendingCount);
                })
                .sorted(Comparator.comparingLong(DoctorWorkloadDto::getPendingCount))
                .collect(Collectors.toList());
    }

    @GetMapping("/create-session")
    public String createSessionPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "patientId", required = false) Integer patientId,
            Model model) {

        if (patientId != null) {
            Patient patient = patientSearchService.getPatientEntityById(patientId);
            if (patient != null) {
                model.addAttribute("selectedPatient", patient);
                model.addAttribute("doctors", getDoctorsWithWorkload());
                // Create an empty request object for the form
                CreateDiagnosisSessionRequest request = new CreateDiagnosisSessionRequest();
                request.setPatientId(patientId);
                model.addAttribute("createRequest", request);
                return "receptionist/create-session";
            } else {
                model.addAttribute("errorMessage", "Không tìm thấy bệnh nhân.");
            }
        }

        if (keyword != null) {
            List<PatientSearchResponse> patients = patientSearchService.searchPatients(keyword);
            model.addAttribute("patients", patients);
            model.addAttribute("keyword", keyword);
            if (patients.isEmpty()) {
                model.addAttribute("infoMessage", "Không tìm thấy bệnh nhân nào phù hợp.");
            }
        }

        return "receptionist/create-session";
    }

    @PostMapping("/create-session")
    public String processCreateSession(
            @Valid @ModelAttribute("createRequest") CreateDiagnosisSessionRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedPatient", patientSearchService.getPatientEntityById(request.getPatientId()));
            model.addAttribute("doctors", getDoctorsWithWorkload());
            return "receptionist/create-session";
        }

        try {
            Integer receptionistId = null;
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                receptionistId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getUserId();
            }

            DiagnosisSessionResponse response = diagnosisSessionService.createSession(request, receptionistId);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo ca khám thành công!");
            redirectAttributes.addFlashAttribute("createdSessionId", response.getSessionId());
            return "redirect:/receptionist/create-session"; // Redirect to clear form and show success

        } catch (Exception e) {
            log.error("Error creating session", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("selectedPatient", patientSearchService.getPatientEntityById(request.getPatientId()));
            model.addAttribute("doctors", getDoctorsWithWorkload());
            return "receptionist/create-session";
        }
    }
}
