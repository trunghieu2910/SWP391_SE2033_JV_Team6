package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.ChangePasswordRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.MessageResponse;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.ProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(
            UserRepository userRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);

        String roleName = getRoleName(user);

        if ("PATIENT".equalsIgnoreCase(roleName)) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

            return mapPatientProfile(user, patient);
        }

        return mapUserProfile(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);

        updateUserFields(user, request);

        String roleName = getRoleName(user);

        if ("PATIENT".equalsIgnoreCase(roleName)) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

            updatePatientFields(patient, request);

            userRepository.save(user);
            patientRepository.save(patient);

            return mapPatientProfile(user, patient);
        }

        userRepository.save(user);

        return mapUserProfile(user);
    }

    @Override
    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new BadRequestException("Tài khoản chưa có mật khẩu hợp lệ");
        }

        boolean oldPasswordMatches = passwordEncoder.matches(
                request.getOldPassword(),
                user.getPasswordHash()
        );

        if (!oldPasswordMatches) {
            throw new BadRequestException("Mật khẩu cũ không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

        user.setPasswordHash(encodedNewPassword);

        userRepository.save(user);

        return MessageResponse.builder()
                .message("Đổi mật khẩu thành công")
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String getRoleName(User user) {
        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            throw new BadRequestException("User chưa có role hợp lệ");
        }

        return user.getRole().getRoleName();
    }

    private void updateUserFields(User user, UpdateProfileRequest request) {
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getCertificate() != null) {
            user.setCertificate(request.getCertificate());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getNationalID() != null) {
            user.setNationalID(request.getNationalID());
        }
    }

    private void updatePatientFields(Patient patient, UpdateProfileRequest request) {
        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }

        if (request.getGender() != null) {
            patient.setGender(request.getGender());
        }

        if (request.getDob() != null) {
            patient.setDob(request.getDob());
        }

        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }

        if (request.getHealthInsurance() != null) {
            patient.setHealthInsurance(request.getHealthInsurance());
        }
    }

    private ProfileResponse mapUserProfile(User user) {
        return ProfileResponse.builder()
                .userID(user.getUserID())
                .patientID(null)
                .roleName(getRoleName(user))
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .certificate(user.getCertificate())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .nationalID(user.getNationalID())
                .firstName(null)
                .lastName(null)
                .gender(null)
                .dob(null)
                .address(null)
                .healthInsurance(null)
                .build();
    }

    private ProfileResponse mapPatientProfile(User user, Patient patient) {
        return ProfileResponse.builder()
                .userID(user.getUserID())
                .patientID(patient.getPatientID())
                .roleName(getRoleName(user))
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .certificate(user.getCertificate())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .nationalID(user.getNationalID())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .gender(patient.getGender())
                .dob(patient.getDob())
                .address(patient.getAddress())
                .healthInsurance(patient.getHealthInsurance())
                .build();
    }
}