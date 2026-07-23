package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.ChangePasswordRequest;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/change-password")
@RequiredArgsConstructor
public class ChangePasswordController {

    private final ProfileService profileService;

    @GetMapping
    public String changePasswordPage(Model model, 
                                     @RequestParam(value = "success", required = false) boolean success,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("passwordForm", new ChangePasswordRequest());
        model.addAttribute("success", success);
        model.addAttribute("backUrl", determineBackUrl(userDetails));
        return "shared/change-password";
    }

    @PostMapping
    public String submitChangePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordRequest form,
                                       BindingResult bindingResult,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest request,
                                       HttpServletResponse response,
                                       Model model) {
        String backUrl = determineBackUrl(userDetails);
        if (bindingResult.hasErrors()) {
            model.addAttribute("backUrl", backUrl);
            return "shared/change-password";
        }

        try {
            profileService.changePassword(userDetails.getUsername(), form);

            User refreshedUser = profileService.getUserByLogin(userDetails.getUsername());
            CustomUserDetails refreshedDetails = new CustomUserDetails(refreshedUser);
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    refreshedDetails, null, refreshedDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(newAuth);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/change-password?success=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("backUrl", backUrl);
            return "shared/change-password";
        }
    }

    private String determineBackUrl(CustomUserDetails userDetails) {
        if (userDetails != null) {
            for (GrantedAuthority authority : userDetails.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN")) {
                    return "/admin/dashboard";
                } else if (role.equals("ROLE_PHARMACIST")) {
                    return "/pharmacist/profile";
                } else if (role.equals("ROLE_DOCTOR")) {
                    return "/doctor/profile";
                } else if (role.equals("ROLE_PATIENT")) {
                    return "/patient/profile";
                }
            }
        }
        return "/"; // default fallback
    }
}
