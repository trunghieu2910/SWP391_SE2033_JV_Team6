package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyOtpRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.*;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.jwt.ResetPasswordJwtService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.service.interfaces.ForgotPasswordService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import com.mycompany.jpademo.backend.util.OtpUtil;
import com.mycompany.jpademo.backend.util.PasswordPolicyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ResetPasswordJwtService resetPasswordJwtService;
    private final SystemLogService systemLogService;

    @Override
    public ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Thông tin email không hợp lệ"));

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);

        emailService.sendOtpEmail(user.getEmail(),user.getFullName(), otp);

        systemLogService.logActivity("Users", user.getUserId(), "FORGOT_PASSWORD",
                "Người dùng gửi yêu cầu quên mật khẩu (" + user.getEmail() + ")");

        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("OTP đã được gửi đến email của bạn")
                        .build());
    }

    @Override
    public ResponseEntity<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request) {
        boolean valid = OtpUtil.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException("no");
        }

        OtpUtil.removeOtp(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).
                orElseThrow(() -> new UserNotFoundException("Thông tin email không hợp lệ"));
        String token = resetPasswordJwtService.generateResetToken(user);

        systemLogService.logActivity("Users", user.getUserId(), "VERIFY_OTP",
                "Người dùng xác minh OTP thành công (" + user.getEmail() + ")");

        return ResponseEntity.ok(VerifyOtpResponse.builder()
                        .resetToken(token)
                        .build());
    }

    @Override
    public ResponseEntity<ApiResponse> resetPassword(ResetPasswordRequest request) {
        if (!resetPasswordJwtService.isValid(request.getResetToken())){
            throw new InvalidResetTokenException();
        }

        if (!PasswordPolicyUtil.isValidPassword(request.getNewPassword())) {
            throw new WeakPasswordException();
        }

        String email = resetPasswordJwtService.extractEmail(request.getResetToken());
        String hashFromToken = resetPasswordJwtService.extractOldHash(request.getResetToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Mã đặt lại không hợp lệ hoặc đã hết hạn"));

        if (!user.getPasswordHash().equals(hashFromToken)) {
            // Nếu không giống, tức là mật khẩu đã được đổi trước đó rồi.
            throw new UseResetTokenAgainException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new DuplicatePasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setLastChangePassTime(LocalDateTime.now());

        userRepository.save(user);

        systemLogService.logActivity("Users", user.getUserId(), "UPDATE_PASSWORD",
                "Người dùng đặt lại mật khẩu thành công (" + user.getEmail() + ")");

        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Mật khẩu đã được đặt lại thành công")
                        .build());
    }
}
