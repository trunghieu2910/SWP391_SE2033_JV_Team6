package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordFormRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyOtpRequest;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import com.mycompany.jpademo.backend.service.interfaces.ForgotPasswordService;
import com.mycompany.jpademo.backend.util.OtpUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordViewController {

    private final ForgotPasswordService forgotPasswordService;

    private static final String SESSION_EMAIL = "fp_email";
    private static final String SESSION_RESET_TOKEN = "fp_reset_token";

    // ── Bước 1: Nhập email, gửi OTP ──────────────────────────────
    @GetMapping
    public String showStep1(Model model) {
        model.addAttribute("form", new ForgotPasswordRequest());
        return "auth/forgot-password-step1";
    }

    @PostMapping
    public String submitStep1(@ModelAttribute("form") ForgotPasswordRequest form,
                              Model model, HttpSession session) {
        try {
            forgotPasswordService.forgotPassword(form);
        } catch (RuntimeException ex) {
            // Chỉ còn bắt lỗi hệ thống thật sự (VD: SMTP server lỗi) — không còn
            // trường hợp "email không tồn tại" nữa vì service không throw cho case đó.
            model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại sau.");
            return "auth/forgot-password-step1";
        }

        session.setAttribute(SESSION_EMAIL, form.getEmail());
        return "redirect:/forgot-password/verify-otp";
    }

    // ── Bước 2: Xác minh OTP ─────────────────────────────────────
    @GetMapping("/verify-otp")
    public String showStep2(Model model, HttpSession session) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("email", email);
        model.addAttribute("remainingSeconds", OtpUtil.getRemainingTime(email)); // ← thêm dòng này
        return "auth/forgot-password-step2";
    }

    @PostMapping("/verify-otp")
    public String submitStep2(@RequestParam String otp,
                              Model model, HttpSession session) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }

        try {
            VerifyOtpRequest request = new VerifyOtpRequest();
            request.setEmail(email);
            request.setOtp(otp);

            VerifyOtpResponse response = forgotPasswordService.verifyOtp(request).getBody();
            session.setAttribute(SESSION_RESET_TOKEN, response.getResetToken());
            return "redirect:/forgot-password/reset";
        } catch (RuntimeException ex) {
            model.addAttribute("email", email);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("remainingSeconds", OtpUtil.getRemainingTime(email)); 
            return "auth/forgot-password-step2";
        }
    }

    // Gửi lại OTP — dùng lại chính bước 1 với email đã lưu trong session
    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);

        forgotPasswordService.forgotPassword(request); // cooldown được xử lý ngầm bên trong service

        redirectAttributes.addFlashAttribute("resent", true);
        return "redirect:/forgot-password/verify-otp";
    }

    // ── Bước 3: Đặt mật khẩu mới ──────────────────────────────────
    @GetMapping("/reset")
    public String showStep3(Model model, HttpSession session) {
        if (session.getAttribute(SESSION_RESET_TOKEN) == null) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("form", new ResetPasswordFormRequest());
        return "auth/forgot-password-step3";
    }

    @PostMapping("/reset")
    public String submitStep3(@ModelAttribute("form") ResetPasswordFormRequest form,
                              Model model, HttpSession session) {
        String resetToken = (String) session.getAttribute(SESSION_RESET_TOKEN);
        if (resetToken == null) {
            return "redirect:/forgot-password";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "auth/forgot-password-step3";
        }

        try {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setResetToken(resetToken);
            request.setNewPassword(form.getNewPassword());

            forgotPasswordService.resetPassword(request);

            session.removeAttribute(SESSION_EMAIL);
            session.removeAttribute(SESSION_RESET_TOKEN);

            return "redirect:/auth/login?resetSuccess=true";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/forgot-password-step3";
        }
    }
}
