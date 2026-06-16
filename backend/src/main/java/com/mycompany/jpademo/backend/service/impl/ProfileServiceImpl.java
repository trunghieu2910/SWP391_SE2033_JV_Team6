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

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public ProfileServiceImpl(
            UserRepository userRepository,
            PatientRepository patientRepository
    ) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public ProfileResponse getProfile(String username) {
        User user = getUserByUsername(username);

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
    public ProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = getUserByUsername(username);

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

    private User getUserByUsername(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
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
}