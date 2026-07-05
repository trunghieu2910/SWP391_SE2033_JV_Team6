package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    // ==================== REGISTER ====================
    
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công. Vui lòng kiểm tra email để nhận OTP xác thực.");
            redirectAttributes.addAttribute("userName", request.getUserName());
            return "redirect:/auth/register/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // ==================== REGISTER - VERIFY OTP ====================
    
    @GetMapping("/register/verify-otp")
    public String showVerifyOtpPage(
            @RequestParam(required = false) String userName,
            Model model
    ) {
        OtpVerificationRequest otpRequest = new OtpVerificationRequest();
        if (userName != null) {
            otpRequest.setUserName(userName);
        }
        model.addAttribute("otpRequest", otpRequest);
        return "auth/verify-otp";
    }

    @PostMapping("/register/verify-otp")
    public String verifyRegistrationOtp(
            @Valid @ModelAttribute("otpRequest") OtpVerificationRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        try {
            authService.verifyRegistrationOtp(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Xác thực OTP thành công. Tài khoản đã kích hoạt.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("otpRequest", request);
            return "auth/verify-otp";
        }
    }

    // ==================== REGISTER - RESEND OTP ====================
    
    @GetMapping("/register/resend-otp")
    public String showResendOtpPage(
            @RequestParam(required = false) String userName,
            Model model
    ) {
        ResendOtpRequest resendRequest = new ResendOtpRequest();
        if (userName != null) {
            resendRequest.setUserName(userName);
        }
        model.addAttribute("resendRequest", resendRequest);
        return "auth/resend-otp";
    }

    @PostMapping("/register/resend-otp")
    public String resendRegistrationOtp(
            @Valid @ModelAttribute("resendRequest") ResendOtpRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/resend-otp";
        }

        try {
            authService.resendRegistrationOtp(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "OTP mới đã được gửi lại. Vui lòng kiểm tra email.");
            redirectAttributes.addAttribute("userName", request.getUserName());
            return "redirect:/auth/register/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("resendRequest", request);
            return "auth/resend-otp";
        }
    }

}