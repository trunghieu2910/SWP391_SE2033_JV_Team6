package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.LoginRequest;
import com.mycompany.jpademo.backend.dto.request.OtpVerificationRequest;
import com.mycompany.jpademo.backend.dto.request.RegisterRequest;
import com.mycompany.jpademo.backend.dto.request.ResendOtpRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyOtpRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    // Convert JSON → LoginRequest
    // Call service
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {

        authService.logout();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Đăng xuất thành công. Toàn bộ Token cũ đã bị vô hiệu hóa!")
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {
        return forgotPasswordService.forgotPassword(request);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Đăng ký thành công. Vui lòng kiểm tra email để nhận OTP xác thực.")
                .build());
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse> verifyRegistrationOtp(
            @Valid
            @RequestBody
            OtpVerificationRequest request
    ) {
        authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Xác thực OTP thành công. Tài khoản đã kích hoạt.")
                .build());
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<ApiResponse> resendRegistrationOtp(
            @Valid
            @RequestBody
            ResendOtpRequest request
    ) {
        authService.resendRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("OTP mới đã được gửi lại. Vui lòng kiểm tra email.")
                .build());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse>
    verifyOtp(
            @Valid
            @RequestBody
            VerifyOtpRequest request
    ) {

        return forgotPasswordService
                .verifyOtp(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse>
    resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {

        return forgotPasswordService
                .resetPassword(request);
    }
}
