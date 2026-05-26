package com.mycompany.jpademo.backend.service.impls;

import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyOtpRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.exception.InvalidResetTokenException;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.exception.WeakPasswordException;
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

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ResetPasswordJwtService resetPasswordJwtService;

    @Override
    public ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Thông tin email không hợp lệ"));

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);

        emailService.sendOtpEmail(user.getEmail(),user.getFullName(), otp);

        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("OTP đã được gửi đến email của bạn")
                        .build());
    }

    @Override
    public ResponseEntity<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request) {
        boolean valid = OtpUtil.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException();
        }

        OtpUtil.removeOtp(request.getEmail());

        String token = resetPasswordJwtService.generateResetToken(request.getEmail());
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Thông tin email không hợp lệ"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Mật khẩu đã được đặt lại thành công")
                        .build());
    }
}
