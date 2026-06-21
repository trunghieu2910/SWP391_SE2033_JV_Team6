package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.request.InitiateCreateDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingDoctorRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.DuplicateResourceException;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import com.mycompany.jpademo.backend.util.OtpUtil;
import com.mycompany.jpademo.backend.cache.PendingDoctorData;
import com.mycompany.jpademo.backend.cache.PendingDoctorStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (request.getStatus() == UserStatus.BANNED) {
            user.setStatus(UserStatus.BANNED);
        } else if (request.getStatus() == UserStatus.ACTIVE){
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user.setStatus(UserStatus.INACTIVE);
        }
        userRepository.save(user);
        sendStatusEmail(user, request.getStatus(), request.getReason());
        return ResponseEntity.ok("User status updated successfully");
    }

    @Override
    @AdminActionLog(action = "CREATE_DOCTOR",
            targetType = "User")
    public ResponseEntity<String> verifyAndCreateDoctor(VerifyPendingDoctorRequest request, User admin) {
        String otp = request.getOtp();
        PendingDoctorData pending = PendingDoctorStore.getPendingByAdminEmail(admin.getEmail());
        if (pending == null) {
            return ResponseEntity.badRequest().body("No pending doctor creation found or it has expired.");
        }
        boolean isOtpValid = OtpUtil.verifyOtp(admin.getEmail(), otp);
        if (!isOtpValid) {
            throw new InvalidOtpException();
        }
        OtpUtil.removeOtp(admin.getEmail());
        PendingDoctorStore.removePending(pending.getRequestId());
        if (userRepository.existsByEmail(pending.getEmail())) {
            throw new DuplicateResourceException("Email is already in use: " + pending.getEmail());
        }
        if (userRepository.findByUserName(pending.getUserName()).isPresent()) {
            throw new DuplicateResourceException("Username is already exists: " + pending.getUserName());
        }
        if (userRepository.existsByPhoneNumber(pending.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number is already in use: " + pending.getPhoneNumber());
        }
        if (pending.getNationalId() != null && userRepository.existsByNationalID(pending.getNationalId())) {
            throw new DuplicateResourceException("National ID is already in use: " + pending.getNationalId());
        }
        User user = new User();
        user.setUserName(pending.getUserName());
        user.setFullName(pending.getFullName());
        user.setEmail(pending.getEmail());
        user.setPhoneNumber(pending.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE);
        user.setNationalID(pending.getNationalId());
        user.setCertificateUrl(pending.getCertificateUrl());
        Role doctorRole = roleRepository.findByRoleName(RoleName.DOCTOR)
                .orElseThrow(() -> new UserNotFoundException("Doctor role not found"));
        user.setRole(doctorRole);

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        emailService.sendEmail(
                user.getEmail(),
                "Tài khoản bác sĩ đã được tạo",
                EmailUtil.buildCreateDoctorAccountTemplate(user.getFullName(), user.getUserName(), rawPassword)
        );
        return ResponseEntity.ok("Doctor created successfully");
    }

    @Override
    public InitiateCreateDoctorResponse initiateCreateDoctor(InitiateCreateDoctorRequest request, User admin) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use: " + request.getEmail());
        }
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new DuplicateResourceException("Username is already exists: " + request.getUserName());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number is already in use: " + request.getPhoneNumber());
        }
        if (request.getNationalId() != null && userRepository.existsByNationalID(request.getNationalId())) {
            throw new DuplicateResourceException("National ID is already in use: " + request.getNationalId());
        }
        String requestId = UUID.randomUUID().toString();
        String certificateUrl = null;
        if (request.getCertificateFile() != null && !request.getCertificateFile().isEmpty()) {
            try {
                String userDir = System.getProperty("user.dir");
                String uploadDir = userDir + File.separator + "uploads" + File.separator + "certificates" + File.separator;

                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + request.getCertificateFile().getOriginalFilename();
                String filePath = uploadDir + fileName;
                request.getCertificateFile().transferTo(new File(filePath));
                certificateUrl = "/uploads/certificates/" + fileName;
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu file bằng cấp: " + e.getMessage());
            }
        }
        PendingDoctorData pending = PendingDoctorData.builder()
                .requestId(requestId)
                .userName(request.getUserName())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .nationalId(request.getNationalId())
                .certificateUrl(certificateUrl)
                .build();
        PendingDoctorStore.savePending(admin.getEmail(), pending);
        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(admin.getEmail(), otp);
        emailService.sendEmail(
                admin.getEmail(),
                "Mã xác thực OTP tạo tài khoản Bác sĩ",
                EmailUtil.buildCreateDoctorOtpForAdmin(admin.getFullName(), otp)
        );
        return InitiateCreateDoctorResponse.builder()
                .requestId(requestId)
                .message("OTP đã được gửi đến email " + admin.getEmail())
                .build();
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalDoctors(userRepository.countByRoleRoleName(RoleName.DOCTOR))
                .totalPatients(userRepository.countByRoleRoleName(RoleName.PATIENT))
                .blockedUsers(userRepository.countByStatus(UserStatus.BANNED))
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

    @Override
    public ChartStatsResponse getChartStats() {
        List<Object[]> userStats = userRepository.getUserRegistrationsByMonth();
        List<Object[]> sessionStats = diagnosisSessionRepository.getDiagnosisSessionsByMonth();

        return ChartStatsResponse.builder()
                .userRegistrations(mapToMonthlyStats(userStats))
                .diagnosisSessions(mapToMonthlyStats(sessionStats))
                .build();
    }

    private List<MonthlyStats> mapToMonthlyStats(List<Object[]> stats) {
        if (stats == null || stats.isEmpty()) {
            return List.of();
        }
        return stats.stream()
                .map(row -> MonthlyStats.builder()
                        .month(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .nationalId(user.getNationalID())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .status(user.getStatus())
                .lastChangePassTime(user.getLastChangePassTime())
                .createdAt(user.getCreatedAt())
                .certificateUrl(user.getCertificateUrl())
                .lastLogoutTime(user.getLastLogoutTime())
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


    private void sendStatusEmail(User user, UserStatus userStatus, String reason) {
        switch (userStatus) {
            case BANNED:
                emailService.sendEmail(
                        user.getEmail(),
                        "Tài khoản của bạn đã bị khoá",
                        EmailUtil.buildBanAccountTemplate(user.getFullName(), reason));
                break;
            case ACTIVE: emailService.sendEmail(
                    user.getEmail(),
                    "Tài khoản của bạn đã được mở khoá",
                    EmailUtil.buildUnbanAccountTemplate(user.getFullName(), reason));
                break;
            case INACTIVE: emailService.sendEmail(
                    user.getEmail(),
                    "Tài khoản của bạn đã được mở khoá",
                    EmailUtil.buildInactiveAccountTemplate(user.getFullName(), reason));
                break;
            default:
                throw new IllegalArgumentException("Unsupported user status: " + userStatus);
        }
    }
}
