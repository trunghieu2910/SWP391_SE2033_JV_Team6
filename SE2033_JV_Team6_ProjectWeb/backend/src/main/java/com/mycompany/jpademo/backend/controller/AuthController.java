package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.OtpVerificationRequest;
import com.mycompany.jpademo.backend.dto.request.RegisterRequest;
import com.mycompany.jpademo.backend.dto.request.ResendOtpRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok(new ApiResponse("Đăng ký thành công. Vui lòng kiểm tra email để nhận OTP xác thực."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        userService.verifyOtp(request);
        return ResponseEntity.ok(new ApiResponse("Xác thực OTP thành công. Tài khoản đã kích hoạt."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        userService.resendOtp(request.getUserName());
        return ResponseEntity.ok(new ApiResponse("OTP mới đã được gửi lại. Vui lòng kiểm tra email."));
    }
}
