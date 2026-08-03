package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.entity.MedicalImageDetails;
import com.mycompany.jpademo.backend.repository.MedicalImageDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@PreAuthorize("hasRole('ULTRASOUND_DOCTOR')")
@RequiredArgsConstructor
public class AiResultViewController {

    private final MedicalImageDetailsRepository repository;

    @GetMapping("/ai-result")
    public String getAiResult(@RequestParam(value = "imageId", required = false) Integer imageId, Model model) {
        if (imageId != null) {
            Optional<MedicalImageDetails> opt = repository.findById(imageId);
            if (opt.isPresent()) {
                model.addAttribute("detail", opt.get());
            }
        }
        return "ultrasound-doctor/ai-result";
    }
}
