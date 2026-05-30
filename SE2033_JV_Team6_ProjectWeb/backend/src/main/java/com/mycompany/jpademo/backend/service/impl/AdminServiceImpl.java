package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.request.CreateDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.response.DashboardStatsResponse;
import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import com.mycompany.jpademo.backend.dto.response.UserDetailResponse;
import com.mycompany.jpademo.backend.dto.response.UserResponse;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.DuplicateResourceException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final SystemLogRepository systemLogRepository;

    private final DiagnosisSessionRepository diagnosisSessionRepository;

    private final EmailService emailService;


    @Override
    public Page<UserResponse> getUser(String keyword, String role, UserStatus status, Pageable pageable) {
        Page<User> users;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasRole = role != null && !role.isBlank();
        boolean hasStatus = status != null;
        if (hasKeyword && hasRole && hasStatus) {
            users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleNameAndStatus(
                    keyword, keyword, RoleName.valueOf(role), status, pageable);
        } else if (hasKeyword && hasRole) {
            users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleName(
                    keyword, keyword, RoleName.valueOf(role), pageable);
        } else if (hasKeyword && hasStatus) {
            users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatus(
                    keyword, keyword, status, pageable);
        } else if (hasRole && hasStatus) {
            users = userRepository.findByRoleRoleNameAndStatus(
                    RoleName.valueOf(role), status, pageable);
        } else if (hasKeyword) {
            users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    keyword, keyword, pageable);
        } else if (hasRole) {
            users = userRepository.findByRoleRoleName(
                    RoleName.valueOf(role), pageable);
        } else if (hasStatus) {
            users = userRepository.findByStatus(
                    status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::mapToUserResponse);
    }

    @Override
    @AdminActionLog(action = "UPDATE_USER_STATUS",
                    targetType = "User")
    public ResponseEntity<String> updateUserStatus(UpdateUserStatusRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + request.getUserId()));
        if (RoleName.ADMIN.equals(user.getRole().getRoleName())) {
            throw new UnauthorizedActionException("Cannot change status of an admin user");
        }
        if (user.getStatus() == request.getStatus()) {
            return ResponseEntity.badRequest()
                    .body("User already has status: " + request.getStatus());
        }
        user.setStatus(request.getStatus());
        userRepository.save(user);
        sendStatusEmail(user, request.getStatus());
        return ResponseEntity.ok("User status updated successfully");
    }

    @Override
    public ResponseEntity<String> createDoctor(CreateDoctorRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use: " + request.getEmail());
        }
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new DuplicateResourceException("Username is already exists: " + request.getUserName());
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE);
        Role doctorRole = roleRepository.findByRoleName(RoleName.DOCTOR)
                .orElseThrow(() -> new UserNotFoundException("Doctor role not found"));
        user.setRole(doctorRole);
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        systemLogRepository.save(
                SystemLog.builder()
                        .user(user)
                        .action("CREATE_DOCTOR")
                        .targetType("User")
                        .targetId(user.getUserId())
                        .description("Admin created doctor account with username: "
                                + user.getUserName())
                        .build()
        );
        emailService.sendEmail(
                user.getEmail(),
                "Tài khoản bác sĩ đã được tạo",
                EmailUtil.buildCreateDoctorAccountTemplate(user.getFullName(),
                        user.getUserName(),
                        rawPassword)
        );
        return ResponseEntity.ok("Doctor created successfully");
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalDoctors(userRepository.countByRoleRoleName(RoleName.DOCTOR))
                .totalPatients(userRepository.countByRoleRoleName(RoleName.PATIENT))
                .blockedUsers(userRepository.countByStatus(UserStatus.BLOCKED))
                .totalDiagnosisSessions(diagnosisSessionRepository.count())
                .build();
    }

    @Override
    public UserDetailResponse getUserDetail(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        List<SystemLog> logs = systemLogRepository.findTop10ByUser_UserIdOrderByPerformedAtDesc(userId);
        return UserDetailResponse.builder()
                .userResponse(mapToUserResponse(user))
                .systemLogResponses(logs.stream().map(this::mapToLogResponse).toList())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .status(user.getStatus())
                .lastChangePassTime(user.getLastChangePassTime())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private SystemLogResponse mapToLogResponse(SystemLog log) {
        return SystemLogResponse.builder()
                .logId(log.getLogId())
                .action(log.getAction())
                .description(log.getDescription())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .performedAt(log.getPerformedAt())
                .build();
    }


    private void sendStatusEmail(User user, UserStatus userStatus) {
        switch (userStatus) {
            case BLOCKED:
                emailService.sendEmail(
                        user.getEmail(),
                        "Tài khoản của bạn đã bị khoá",
                        EmailUtil.buildBanAccountTemplate(user.getFullName()));
                break;
            case ACTIVE: emailService.sendEmail(
                    user.getEmail(),
                    "Tài khoản của bạn đã được mở khoá",
                    EmailUtil.buildUnbanAccountTemplate(user.getFullName()));
                break;
            default:
                throw new IllegalArgumentException("Unsupported user status: " + userStatus);
        }
    }
}
