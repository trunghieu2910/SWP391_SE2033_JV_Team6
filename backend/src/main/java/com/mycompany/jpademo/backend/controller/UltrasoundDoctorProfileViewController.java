package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/ultrasound-doctor/profile")
@PreAuthorize("hasRole('ULTRASOUND_DOCTOR')")
@RequiredArgsConstructor
public class UltrasoundDoctorProfileViewController {

    private final ProfileService profileService;

    @GetMapping
    public String profilePage(Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "success", required = false) boolean success) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        UpdateProfileRequest profileForm = UpdateProfileRequest.builder()
                .username(profile.getUsername())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .nationalID(profile.getNationalID())
                .gender(profile.getGender())
                .dob(profile.getDob())
                .address(profile.getAddress())
                .build();

        model.addAttribute("profile", profile);
        model.addAttribute("profileForm", profileForm);
        model.addAttribute("success", success);
        model.addAttribute("today", LocalDate.now());

        return "ultrasound-doctor/profile";
    }

    @PostMapping
    public String saveProfile(@Valid @ModelAttribute("profileForm") UpdateProfileRequest profileForm,
                               BindingResult bindingResult,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("profileForm", profileForm);
            return "ultrasound-doctor/profile";
        }

        profileService.updateProfile(userDetails.getUsername(), profileForm);
        redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công.");
        return "redirect:/ultrasound-doctor/profile?success=true";
    }
}
