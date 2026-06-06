package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.request.LoginRequest;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.factory.AuthFactory;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.LoginStrategy;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mycompany.jpademo.backend.dto.request.OtpVerificationRequest;
import com.mycompany.jpademo.backend.dto.request.RegisterRequest;
import com.mycompany.jpademo.backend.dto.request.ResendOtpRequest;
import com.mycompany.jpademo.backend.exception.ResourceAlreadyExistsException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.util.OtpUtil;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthFactory authFactory;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @LogActivity(action = "LOGIN", targetType = "Users", description = "Người dùng đăng nhập")
    public LoginResponse login(LoginRequest request) {
        // Verify login/password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getLogin(),
                request.getPassword())
        );

        // Query the user again to retrieve the full information.
        User user = userRepository.findByEmailOrUsernameOrPhoneNumberOrNationalId(
                request.getLogin(),
                request.getLogin(),
                request.getLogin(),
                request.getLogin()
        ).orElseThrow(() -> new UserNotFoundException("Thông tin đăng nhập không hợp lệ"));

        // Choose a strategy based on role - convert RoleName enum to String
        LoginStrategy strategy = authFactory.getLoginStrategy(user.getRole().getRoleName().name());

        return strategy.login(user);
    }

    @Override
    @LogActivity(action = "LOGOUT", targetType = "Users", description = "Người dùng đăng xuất")
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userDetails.getUser();


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

        Patient patient = new Patient();
        patient.setGender(request.getGender());
        patient.setDob(request.getDob());
        patient.setAddress(request.getAddress());
        patient.setUser(user);
        user.setPatient(patient);

        userRepository.save(user);

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
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
}
