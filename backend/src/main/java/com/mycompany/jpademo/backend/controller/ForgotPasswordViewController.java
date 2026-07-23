package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordFormRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyOtpRequest;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import com.mycompany.jpademo.backend.service.interfaces.ForgotPasswordService;
import com.mycompany.jpademo.backend.util.OtpUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Server-rendered (Thymeleaf) 3-step "forgot password" wizard:
 * <ol>
 *   <li>enter email -> request OTP</li>
 *   <li>enter OTP -> exchange for a short-lived reset token</li>
 *   <li>enter new password, authorized by that reset token</li>
 * </ol>
 * State between steps is kept in the HTTP session ({@link #SESSION_EMAIL},
 * {@link #SESSION_RESET_TOKEN}) rather than in the URL, so a step cannot be
 * skipped by guessing/bookmarking a URL.
 */
@Controller
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordViewController {

    private final ForgotPasswordService forgotPasswordService;

    private static final String SESSION_EMAIL = "fp_email";
    private static final String SESSION_RESET_TOKEN = "fp_reset_token";

    /** Renders the step-1 form (email input). */
    @GetMapping
    public String showStep1(Model model) {
        model.addAttribute("form", new ForgotPasswordRequest());
        return "auth/forgot-password-step1";
    }

    /**
     * Handles step-1 submission: triggers OTP generation/email via
     * {@link ForgotPasswordService#forgotPassword}, stores the email in the
     * session for the next steps, and moves on to step 2.
     */
    @PostMapping
    public String submitStep1(@Valid @ModelAttribute("form") ForgotPasswordRequest form,
                              BindingResult bindingResult,
                              Model model, HttpSession session) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getFieldError().getDefaultMessage());
            return "auth/forgot-password-step1";
        }

        try {
            forgotPasswordService.forgotPassword(form);
        } catch (RuntimeException ex) {
            model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại sau.");
            return "auth/forgot-password-step1";
        }

        session.setAttribute(SESSION_EMAIL, form.getEmail());
        return "redirect:/forgot-password/verify-otp";
    }

    /**
     * Renders the step-2 form (OTP input). Redirects back to step 1 if the
     * session has no email yet (step was reached out of order).
     */
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

    /**
     * Handles step-2 submission: verifies the OTP via
     * {@link ForgotPasswordService#verifyOtp}, stores the resulting reset
     * token in the session, and moves on to step 3.
     */
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

    /**
     * Re-triggers OTP generation/email for the email already stored in the
     * session (reuses step-1 logic; cooldown is enforced inside the service).
     */
    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute(SESSION_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }

        try {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail(email);

            forgotPasswordService.forgotPassword(request);

            redirectAttributes.addFlashAttribute("resent", true);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra, vui lòng thử lại sau.");
        }

        return "redirect:/forgot-password/verify-otp";
    }

    /**
     * Renders the step-3 form (new password input). Redirects back to step
     * 1 if there is no reset token in the session (step was reached out of order).
     */
    @GetMapping("/reset")
    public String showStep3(Model model, HttpSession session) {
        if (session.getAttribute(SESSION_RESET_TOKEN) == null) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("form", new ResetPasswordFormRequest());
        return "auth/forgot-password-step3";
    }

    /**
     * Handles step-3 submission: validates the new/confirm password match,
     * applies the change via {@link ForgotPasswordService#resetPassword},
     * clears the wizard's session state, and redirects to the login page.
     */
    @PostMapping("/reset")
    public String submitStep3(@Valid @ModelAttribute("form") ResetPasswordFormRequest form,
                              BindingResult bindingResult,
                              Model model, HttpSession session) {

        String resetToken = (String) session.getAttribute(SESSION_RESET_TOKEN);
        if (resetToken == null) {
            return "redirect:/forgot-password";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getFieldError().getDefaultMessage());
            return "auth/forgot-password-step3";
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
