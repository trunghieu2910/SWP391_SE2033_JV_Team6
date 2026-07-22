package com.mycompany.jpademo.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('ULTRASOUND_DOCTOR')")
public class AiResultViewController {

    @GetMapping("/ai-result")
    public String getAiResult() {
        return "ultrasound-doctor/ai-result";
    }
}
