package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.response.UserRespone;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.DoctorApprovalException;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final SystemLogRepository systemLogRepository;

    @Override
    public List<UserRespone> getAllUser() {
        List<User> users = userRepository.findAll();
        return getUserRespones(users);
    }

    @Override
    public List<UserRespone> searchUsers(String keyword) {
        List<User> users = new ArrayList<>();
        users.addAll(userRepository.findByUsernameContaining(keyword));
        users.addAll(userRepository.findByEmailContaining(keyword));
        return getUserRespones(users);
    }

    @Override
    @AdminActionLog(action = "BAN_USER",
                    targetType = "User")
    public ResponseEntity<String> banUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        return ResponseEntity.ok("User banned successfully");
    }

    @Override
    @AdminActionLog(action = "UNBAN_USER",
                    targetType = "User")
    public ResponseEntity<String> unbanUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return ResponseEntity.ok("User unbanned successfully");
    }

    @Override
    public List<UserRespone> getPendingDoctors() {
        List<User> users = userRepository.findByRoleRoleNameAndStatus("DOCTOR", "PENDING");
        return getUserRespones(users);
    }

    @Override
    @AdminActionLog(action = "APPROVE_DOCTOR",
                    targetType = "User")
    public ResponseEntity<String> approveDoctor(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (!user.getRole().getRoleName().equals("DOCTOR")) {
            throw new DoctorApprovalException("User is not a doctor");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return ResponseEntity.ok("Doctor approved successfully");
    }

    @Override
    public ResponseEntity<String> rejectDoctor(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (!user.getRole().getRoleName().equals("DOCTOR")) {
            throw new DoctorApprovalException("User is not a doctor");
        }
        user.setStatus(UserStatus.REJECTED);
        userRepository.save(user);
        return ResponseEntity.ok("Doctor rejected successfully");
    }

    @NonNull
    private List<UserRespone> getUserRespones(List<User> users) {
        List<UserRespone> respones = new ArrayList<>();
        for (User user: users) {
            UserRespone respone = UserRespone.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .roleName(user.getRole().getRoleName())
                    .status(user.getStatus())
                    .certificate(user.getCertificate())
                    .createdAt(user.getCreatedAt())
                    .build();
            respones.add(respone);
        }
        return respones;
    }
}
