package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/rate-limit")
    public String rateLimitError(
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        String originalUrl = redirect;
        if (originalUrl == null || originalUrl.isEmpty()) {
            originalUrl = (String) session.getAttribute("originalUrl");
        }

        if (originalUrl == null || originalUrl.isEmpty()) {
            originalUrl = request.getHeader("Referer");
        }

        if (originalUrl == null || originalUrl.isEmpty()) {
            originalUrl = getDefaultRedirectUrl();
        }

        model.addAttribute("redirectUrl", originalUrl);
        return "error/rate-limit";
    }

    private String getDefaultRedirectUrl() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
                String role = userDetails.getUser().getRole().getRoleName().name();

                switch (role) {
                    case "ADMIN":
                        return "/admin/dashboard";
                    case "DOCTOR":
                        return "/doctor/sessions";
                    case "PATIENT":
                        return "/patient/dashboard";
                    default:
                        return "/";
                }
            }
        } catch (Exception e) {
            // Bỏ qua lỗi
        }
        return "/";
    }
}