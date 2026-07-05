package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
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
    public void register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new ResourceAlreadyExistsException("Username đang được sử dụng.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email đang được sử dụng.");
        }
        if (userRepository.existsByNationalID(request.getNationalID())) {
            throw new ResourceAlreadyExistsException("Số CMND/CCCD đã tồn tại.");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Số điện thoại đang được sử dụng.");
        }

        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT không tồn tại."));

        User user = new User();
        user.setUserName(request.getUserName());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalID(request.getNationalID());
        user.setStatus(UserStatus.PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(patientRole);

        userRepository.save(user);

        Patient patient = new Patient();
        patient.setGender(request.getGender());
        patient.setDob(request.getDob());
        patient.setAddress(request.getAddress());
        patient.setUser(user);
        
        patientRepository.save(patient);

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    public void verifyRegistrationOtp(OtpVerificationRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new InvalidOtpException("no");
        }

        boolean valid = OtpUtil.verifyOtp(user.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException("no");
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
            throw new InvalidOtpException("no");
        }

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

}
