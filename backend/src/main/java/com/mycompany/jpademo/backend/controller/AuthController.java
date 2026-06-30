package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // ==================== LOGIN ====================
    
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            LoginResponse response = authService.login(request);
            // Lưu token vào session hoặc cookie
            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    // ==================== LOGOUT ====================
    
    @GetMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        authService.logout();
        redirectAttributes.addFlashAttribute("successMessage", 
            "Đăng xuất thành công. Toàn bộ Token cũ đã bị vô hiệu hóa!");
        return "redirect:/auth/login";
    }

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

    // ==================== FORGOT PASSWORD ====================
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            ResponseEntity<ApiResponse> response = forgotPasswordService.forgotPassword(request);
            if (Boolean.TRUE.equals(response.getBody().getSuccess())) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    response.getBody().getMessage());
                redirectAttributes.addFlashAttribute("email", request.getEmail());
                return "redirect:/auth/forgot-password/verify-otp";
            } else {
                model.addAttribute("error", response.getBody().getMessage());
                return "auth/forgot-password";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/forgot-password";
        }
    }

    // ==================== FORGOT PASSWORD - VERIFY OTP ====================
    
    @GetMapping("/forgot-password/verify-otp")
    public String showForgotPasswordVerifyOtpPage(
            @RequestParam(required = false) String email,
            Model model
    ) {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        if (email != null) {
            verifyOtpRequest.setEmail(email);
        }
        model.addAttribute("verifyOtpRequest", verifyOtpRequest);
        return "auth/forgot-password-verify-otp";
    }

    @PostMapping("/forgot-password/verify-otp")
    public String verifyOtp(
            @Valid @ModelAttribute("verifyOtpRequest") VerifyOtpRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password-verify-otp";
        }

        try {
            ResponseEntity<VerifyOtpResponse> response = forgotPasswordService.verifyOtp(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Mã OTP hợp lệ. Vui lòng đặt lại mật khẩu.");
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("token", response.getBody().getResetToken());
            return "redirect:/auth/forgot-password/reset-password";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/forgot-password-verify-otp";
        }
    }

    // ==================== FORGOT PASSWORD - RESET PASSWORD ====================
    
    @GetMapping("/forgot-password/reset-password")
    public String showResetPasswordPage(
            @RequestParam(required = false) String token,
            Model model
    ) {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        if (token != null) {
            resetRequest.setResetToken(token);
        }
        model.addAttribute("resetRequest", resetRequest);
        return "auth/reset-password";
    }

    @PostMapping("/forgot-password/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute("resetRequest") ResetPasswordRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            ResponseEntity<ApiResponse> response = forgotPasswordService.resetPassword(request);
            if (Boolean.TRUE.equals(response.getBody().getSuccess())) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    response.getBody().getMessage());
                return "redirect:/auth/login";
            } else {
                model.addAttribute("error", response.getBody().getMessage());
                return "auth/reset-password";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
    }

    // ==================== GOOGLE LOGIN ====================
    
    @GetMapping("/google")
    public String showGoogleLoginPage(Model model) {
        model.addAttribute("googleLoginRequest", new GoogleLoginRequest());
        return "auth/google-login";
    }

    @PostMapping("/google")
    public String googleLogin(
            @Valid @ModelAttribute("googleLoginRequest") GoogleLoginRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/google-login";
        }

        try {
            Object result = authService.handleGoogleLogin(request.getIdToken());
            
            if (result instanceof LoginResponse) {
                // User đã tồn tại -> Đăng nhập thành công
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Đăng nhập bằng Google thành công!");
                return "redirect:/home";
            } else {
                // User chưa tồn tại -> Chuyển đến trang hoàn tất đăng ký
                redirectAttributes.addFlashAttribute("googleData", result);
                redirectAttributes.addFlashAttribute("idToken", request.getIdToken());
                return "redirect:/auth/google/complete";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/google-login";
        }
    }

    // ==================== GOOGLE COMPLETE REGISTRATION ====================
    
    @GetMapping("/google/complete")
    public String showGoogleCompletePage(Model model) {
        model.addAttribute("googleCompleteRequest", new GoogleCompleteRequest());
        return "auth/google-complete";
    }

    @PostMapping("/google/complete")
    public String googleComplete(
            @Valid @ModelAttribute("googleCompleteRequest") GoogleCompleteRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/google-complete";
        }

        try {
            LoginResponse response = authService.completeGoogleRegistration(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký và đăng nhập bằng Google thành công!");
            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/google-complete";
        }
    }
}