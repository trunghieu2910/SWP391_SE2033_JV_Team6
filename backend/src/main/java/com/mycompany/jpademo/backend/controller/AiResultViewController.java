package com.mycompany.jpademo.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('TECHNICAL')")
public class AiResultViewController {

    @GetMapping("/ai-result")
    public String getAiResult() {
        return "technical/ai-result";
    }
}
