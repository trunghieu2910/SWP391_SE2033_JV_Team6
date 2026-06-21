package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.aop.context.AuditLogContext;
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

    @Override
    @LogActivity(action = "FORGOT_PASSWORD", targetType = "Users", description = "Người dùng gửi yêu cầu quên mật khẩu")
    public ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Thông tin email không hợp lệ"));

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);

        emailService.sendOtpEmail(user.getEmail(),user.getFullName(), otp);

        AuditLogContext.setTargetId(user.getUserId());

        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("OTP đã được gửi đến email của bạn")
                        .build());
    }

    @Override
    @LogActivity(action = "VERIFY_OTP", targetType = "Users", description = "Người dùng xác minh OTP")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request) {
        boolean valid = OtpUtil.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException();
        }

        OtpUtil.removeOtp(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).
                orElseThrow(() -> new UserNotFoundException("Thông tin email không hợp lệ"));
        String token = resetPasswordJwtService.generateResetToken(user);

        AuditLogContext.setTargetId(user.getUserId());

        return ResponseEntity.ok(VerifyOtpResponse.builder()
                        .resetToken(token)
                        .build());
    }

    @Override
    @LogActivity(action = "UPDATE_PASSWORD", targetType = "Users", description = "Người dùng đặt lại mật khẩu")
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

        AuditLogContext.setTargetId(user.getUserId());

        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Mật khẩu đã được đặt lại thành công")
                        .build());
    }
}
