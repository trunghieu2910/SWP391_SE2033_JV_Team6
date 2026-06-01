package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.LoginRequest;
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
