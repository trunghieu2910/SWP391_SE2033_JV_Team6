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

/**
 * Default implementation of {@link ForgotPasswordService}.
 */
@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ResetPasswordJwtService resetPasswordJwtService;
    private final SystemLogService systemLogService;

    /**
     * {@inheritDoc}
     * <p>
     * Deliberately timing- and response-indistinguishable for existing vs.
     * non-existing emails: an OTP is always generated and stored (even for
     * emails with no account), and the same generic response is always
     * returned, so this endpoint cannot be used to enumerate registered
     * emails. A cooldown (based on {@link OtpUtil#getRemainingTime}) also
     * prevents spamming a new OTP while a previous one is still valid.
     */
    @Override
    public ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordRequest request) {

        String email = request.getEmail();

        // ── Fix #2: Cooldown — nếu email này vừa có OTP còn hiệu lực thì không gửi lại ──
        // Áp dụng TRƯỚC khi kiểm tra email có tồn tại hay không, để hành vi
        // giống hệt nhau dù email thật hay giả (tránh lộ thông tin qua timing).
        if (OtpUtil.getRemainingTime(email) > 0) {
            return ResponseEntity.ok(genericOtpSentResponse());
        }

        // ── Fix #1: Luôn sinh & lưu OTP, bất kể email có tồn tại hay không ──
        // Việc lưu "OTP ma" cho email không tồn tại đảm bảo cooldown/đồng hồ đếm ngược
        // ở Bước 2 hoạt động giống hệt nhau ở cả 2 trường hợp → không thể phân biệt được.
        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(email, otp);

        userRepository.findByEmail(email).ifPresent(user -> {
            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
            systemLogService.logActivity("Users", user.getUserId(), "FORGOT_PASSWORD",
                    "Người dùng gửi yêu cầu quên mật khẩu (" + user.getEmail() + ")");
        });
        // Nếu email không tồn tại: KHÔNG gửi mail, KHÔNG ghi log — nhưng response
        // và trạng thái OTP nội bộ vẫn giống hệt trường hợp thành công.

        return ResponseEntity.ok(genericOtpSentResponse());
    }

    private ApiResponse genericOtpSentResponse() {
        return ApiResponse.builder()
                .success(true)
                .message("Nếu email tồn tại trong hệ thống, mã OTP đã được gửi")
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * On success, the OTP is immediately invalidated (single use) and
     * exchanged for a short-lived JWT reset token bound to this email and
     * the user's current password hash (see {@link ResetPasswordJwtService}).
     */
    @Override
    public ResponseEntity<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request) {
        boolean valid = OtpUtil.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException();
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

    /**
     * {@inheritDoc}
     * <p>
     * The reset token embeds the password hash that was current at the time
     * the OTP was verified; if it no longer matches the user's current hash,
     * the password was already changed since (e.g. via a second concurrent
     * reset), so the token is rejected as already used.
     */
    @Override
    public ResponseEntity<ApiResponse> resetPassword(ResetPasswordRequest request) {

        if (!resetPasswordJwtService.isValid(request.getResetToken())
                || !resetPasswordJwtService.isResetPasswordToken(request.getResetToken())) {
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
