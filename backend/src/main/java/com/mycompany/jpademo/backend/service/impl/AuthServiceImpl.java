package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import jakarta.transaction.Transactional;
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

import com.mycompany.jpademo.backend.cache.PendingRegistrationStore;

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



        PendingRegistrationStore.savePending(request.getUserName(), request);

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(request.getEmail(), otp);
        emailService.sendOtpEmail(request.getEmail(), request.getFullName(), otp);
    }

    @Override
    @Transactional
    @LogActivity(action = "REGISTER", targetType = "Users", description = "Đăng ký tài khoản Bệnh nhân mới (Lễ tân tạo)")
    public void registerByReceptionist(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new ResourceAlreadyExistsException("Tên đăng nhập đã tồn tại.");
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
        // Lễ tân tạo thì ACTIVE luôn
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(patientRole);

        userRepository.save(user);

        Patient patient = new Patient();
        patient.setGender(request.getGender());
        patient.setDob(request.getDob());
        patient.setAddress(request.getAddress());
        patient.setUser(user);
        
        patientRepository.save(patient);
        // KHÔNG GỬI OTP EMAIL
    }

    @Override
    @Transactional
    @LogActivity(action = "REGISTER", targetType = "Users", description = "Đăng ký tài khoản Bệnh nhân mới (Bệnh nhân tự đăng ký)")
    public void verifyRegistrationOtp(OtpVerificationRequest request) {
        //Lấy thông tin đăng ký tạm thời đã lưu ở bước trước đó từ PendingRegistrationStore.
        RegisterRequest registerRequest = PendingRegistrationStore.getPending(request.getUserName());
        if (registerRequest == null) {
            throw new ResourceNotFoundException("Thông tin đăng ký không tồn tại hoặc đã hết hạn.");
        }

        //Xác thực mã OTP
        boolean valid = OtpUtil.verifyOtp(registerRequest.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException("OTP không hợp lệ.");
        }

        //check role patient đã tồn tại hay chưa
        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT không tồn tại."));

        //Tạo và Lưu tài khoản Người dùng
        User user = new User();
        user.setUserName(registerRequest.getUserName());
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setNationalID(registerRequest.getNationalID());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(patientRole);
        userRepository.save(user);

        Patient patient = new Patient();
        patient.setGender(registerRequest.getGender());
        patient.setDob(registerRequest.getDob());
        patient.setAddress(registerRequest.getAddress());
        patient.setUser(user);
        patientRepository.save(patient);

        OtpUtil.removeOtp(registerRequest.getEmail());
        PendingRegistrationStore.removePending(request.getUserName());
    }

    @Override
    public void resendRegistrationOtp(ResendOtpRequest request) {
        RegisterRequest registerRequest = PendingRegistrationStore.getPending(request.getUserName());
        if (registerRequest == null) {
            throw new ResourceNotFoundException("Thông tin đăng ký không tồn tại hoặc đã hết hạn.");
        }

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(registerRequest.getEmail(), otp);
        emailService.sendOtpEmail(registerRequest.getEmail(), registerRequest.getFullName(), otp);
    }

}
