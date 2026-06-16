package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<VerifyOtpResponse>
    verifyOtp(
            @Valid
            @RequestBody
            VerifyOtpRequest request
    ) {

        return forgotPasswordService
                .verifyOtp(request);
    }

    @PostMapping("/forgot-password/reset-password")
    public ResponseEntity<ApiResponse>
    resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {

        return forgotPasswordService
                .resetPassword(request);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(
            @Valid
            @RequestBody
            GoogleLoginRequest request) {

        Object result = authService.handleGoogleLogin(request.getIdToken());

        if (result instanceof LoginResponse) {
            // User đã tồn tại -> Trả về JWT (HTTP 200 OK)
            return ResponseEntity.ok(result);
        } else {
            // User chưa tồn tại -> Trả về yêu cầu nhập thêm thông tin (HTTP 202 Accepted)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
        }
    }

    @PostMapping("/google/complete")
    public ResponseEntity<LoginResponse> googleComplete(
            @Valid
            @RequestBody
            GoogleCompleteRequest request) {

        // Đăng ký hoàn tất và trả thẳng về JWT token (HTTP 200 OK)
        LoginResponse response = authService.completeGoogleRegistration(request);
        return ResponseEntity.ok(response);
    }
}
