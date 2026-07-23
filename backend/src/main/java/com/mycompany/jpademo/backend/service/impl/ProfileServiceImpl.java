package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

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
    public ProfileResponse getProfile(String login) {
        User user = getUserByLogin(login);

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
    public ProfileResponse updateProfile(String login, UpdateProfileRequest request) {
        User user = getUserByLogin(login);

        updateUserFields(user, request);

        String roleName = getRoleName(user);

        if ("PATIENT".equalsIgnoreCase(roleName)) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

            updatePatientFields(patient, request);

            userRepository.save(user);
            patientRepository.save(patient);

            updateSessionUser(user);

            return mapPatientProfile(user, patient);
        }

        userRepository.save(user);

        updateSessionUser(user);

        return mapUserProfile(user);
    }

    @Override
    @Transactional
    public void changePassword(String login, com.mycompany.jpademo.backend.dto.request.ChangePasswordRequest request) {
        User user = getUserByLogin(login);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setLastChangePassTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User getUserByLogin(String login) {
        return userRepository.findByEmailOrUsernameOrPhoneNumberOrNationalId(
                login, login, login, login
        ).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với thông tin định danh đăng nhập: " + login));
    }

    private String getRoleName(User user) {
        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            throw new BadRequestException("User chưa có role hợp lệ");
        }
        // RoleName là Enum, trả về tên String của nó
        return user.getRole().getRoleName().name();
    }

    private void updateUserFields(User user, UpdateProfileRequest request) {
        if (request.getUsername() != null) {
            user.setUserName(request.getUsername());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }



        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getNationalID() != null) {
            user.setNationalID(request.getNationalID());
        }
    }

    private void updatePatientFields(Patient patient, UpdateProfileRequest request) {


        if (request.getGender() != null) {
            patient.setGender(request.getGender());
        }

        if (request.getDob() != null) {
            patient.setDob(request.getDob());
        }

        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }


    }

    private ProfileResponse mapUserProfile(User user) {
        return ProfileResponse.builder()
                .userID(user.getUserId())
                .patientID(null)
                .roleName(getRoleName(user))
                .username(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())

                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .nationalID(user.getNationalID())

                .gender(null)
                .dob(null)
                .address(null)

                .build();
    }

    private ProfileResponse mapPatientProfile(User user, Patient patient) {
        return ProfileResponse.builder()
                .userID(user.getUserId())
                .patientID(patient.getPatientId())
                .roleName(getRoleName(user))
                .username(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())

                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .nationalID(user.getNationalID())

                .gender(patient.getGender())
                .dob(patient.getDob())
                .address(patient.getAddress())

                .build();
    }

    private void updateSessionUser(User updatedUser) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
                User sessionUser = userDetails.getUser();
                if (sessionUser != null && sessionUser.getUserId().equals(updatedUser.getUserId())) {
                    sessionUser.setFullName(updatedUser.getFullName());
                    sessionUser.setUserName(updatedUser.getUserName());
                    sessionUser.setPhoneNumber(updatedUser.getPhoneNumber());
                    sessionUser.setNationalID(updatedUser.getNationalID());
                }
            }
        } catch (Exception e) {
            // Ignore if security context is not available or exception occurs
        }
    }


}