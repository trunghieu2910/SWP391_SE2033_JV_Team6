package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.OtpVerificationRequest;
import com.mycompany.jpademo.backend.dto.request.RegisterRequest;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.exception.ResourceAlreadyExistsException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.service.interfaces.OtpStore;
import com.mycompany.jpademo.backend.service.interfaces.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final OtpStore otpStore;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           EmailService emailService,
                           OtpStore otpStore,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.otpStore = otpStore;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
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

        Role patientRole = roleRepository.findByRoleName("PATIENT")
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT không tồn tại."));

        User user = new User();
        user.setUserName(request.getUserName());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalID(request.getNationalID());
        user.setStatus("PENDING");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(patientRole);

        Patient patient = new Patient();
        patient.setGender(request.getGender());
        patient.setDob(request.getDob());
        patient.setAddress(request.getAddress());
        patient.setUser(user);
        user.setPatient(patient);

        userRepository.save(user);

        String otp = otpStore.generateOtp(request.getUserName());
        emailService.sendOtp(request.getEmail(), request.getFullName(), otp);
    }

    @Override
    @Transactional
    public void verifyOtp(OtpVerificationRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (!"PENDING".equalsIgnoreCase(user.getStatus())) {
            throw new InvalidOtpException("Tài khoản đã được xác thực hoặc không hợp lệ.");
        }

        if (!otpStore.validateOtp(request.getUserName(), request.getOtp())) {
            throw new InvalidOtpException("OTP không hợp lệ hoặc đã hết hạn.");
        }

        user.setStatus("ACTIVE");
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendOtp(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (!"PENDING".equalsIgnoreCase(user.getStatus())) {
            throw new InvalidOtpException("Chỉ có thể yêu cầu lại OTP khi tài khoản đang chờ xác thực.");
        }

        String otp = otpStore.generateOtp(userName);
        emailService.sendOtp(user.getEmail(), user.getFullName(), otp);
    }
}

