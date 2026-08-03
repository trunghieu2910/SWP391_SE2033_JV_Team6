package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/ultrasound-doctor")
@PreAuthorize("hasRole('ULTRASOUND_DOCTOR')")
@RequiredArgsConstructor
public class UltrasoundDoctorController {

    private final DiagnosisSessionService diagnosisSessionService;

    @GetMapping({"", "/", "/dashboard"})
    public String getDashboard(Model model) {
        List<DiagnosisSessionResponse> pendingSessions = diagnosisSessionService.getPendingUltrasoundSessions();
        model.addAttribute("sessions", pendingSessions);
        return "ultrasound-doctor/dashboard";
    }

    @GetMapping("/history")
    public String getHistory(Model model) {
        List<DiagnosisSessionResponse> completedSessions = diagnosisSessionService.getCompletedUltrasoundSessions();
        model.addAttribute("sessions", completedSessions);
        return "ultrasound-doctor/history";
    }

    @GetMapping("/ultrasound-simulator")
    public String getUltrasoundSimulator(@RequestParam(value = "sessionId", required = false) Integer sessionId, Model model) {
        if (sessionId != null) {
            model.addAttribute("sessionId", sessionId);
        }
        return "ultrasound-doctor/ultrasound-simulator";
    }
}
