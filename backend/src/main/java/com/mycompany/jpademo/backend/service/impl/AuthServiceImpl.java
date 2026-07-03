package com.mycompany.jpademo.backend.service.impl;

import com.google.firebase.auth.FirebaseToken;
import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.FirebaseService;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mycompany.jpademo.backend.exception.ResourceAlreadyExistsException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.util.OtpUtil;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final FirebaseService firebaseService;

    @Override
    @LogActivity(action = "LOGOUT", targetType = "Users", description = "Người dùng đăng xuất")
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User currentUser = userDetails.getUser();

            currentUser.setLastLogoutTime(LocalDateTime.now());
            userRepository.save(currentUser);
        }

        SecurityContextHolder.clearContext();
    }

    @Override
    public void verifyRegistrationOtp(OtpVerificationRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new InvalidOtpException();
        }

        boolean valid = OtpUtil.verifyOtp(user.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException();
        }

        OtpUtil.removeOtp(user.getEmail());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public void resendRegistrationOtp(ResendOtpRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new InvalidOtpException();
        }

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

    // Hàm phụ trợ sinh mật khẩu 12 ký tự đảm bảo độ mạnh (Chữ hoa, thường, số, ký tự đặc biệt)
    private String generateRandomPassword() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // Kết hợp 8 ký tự ngẫu nhiên từ UUID + 4 ký tự cứng cứng chuẩn quy tắc
        return uuid.substring(0, 8) + "X@1a";
    }
}
