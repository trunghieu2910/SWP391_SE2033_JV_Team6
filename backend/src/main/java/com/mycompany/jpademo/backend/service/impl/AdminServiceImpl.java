package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.request.InitiateCreateDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingDoctorRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.DuplicateResourceException;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import com.mycompany.jpademo.backend.util.OtpUtil;
import com.mycompany.jpademo.backend.cache.PendingDoctorData;
import com.mycompany.jpademo.backend.cache.PendingDoctorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final SystemLogRepository systemLogRepository;

    private final DiagnosisSessionRepository diagnosisSessionRepository;

    private final EmailService emailService;

    private final BlockedIPRepository blockedIPRepository;

    @Override
    public Page<UserResponse> getUser(String keyword, String role, UserStatus status,
                                      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        Page<User> users;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasRole = role != null && !role.isBlank();
        boolean hasStatus = status != null;
        boolean hasDateFilter = startDate != null && endDate != null;
        if (hasDateFilter) {
            if (hasKeyword && hasRole && hasStatus) {
                users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleNameAndStatusAndCreatedAtBetween(
                        keyword, keyword, RoleName.valueOf(role), status, startDate, endDate, pageable);
            } else if (hasKeyword && hasRole) {
                users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleNameAndCreatedAtBetween(
                        keyword, keyword, RoleName.valueOf(role), startDate, endDate, pageable);
            } else if (hasKeyword && hasStatus) {
                users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatusAndCreatedAtBetween(
                        keyword, keyword, status, startDate, endDate, pageable);
            } else if (hasRole && hasStatus) {
                users = userRepository.findByRoleRoleNameAndStatusAndCreatedAtBetween(
                        RoleName.valueOf(role), status, startDate, endDate, pageable);
            } else if (hasKeyword) {
                users = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCreatedAtBetween(
                        keyword, keyword, startDate, endDate, pageable);
            } else if (hasRole) {
                users = userRepository.findByRoleRoleNameAndCreatedAtBetween(
                        RoleName.valueOf(role), startDate, endDate, pageable);
            } else if (hasStatus) {
                users = userRepository.findByStatusAndCreatedAtBetween(
                        status, startDate, endDate, pageable);
            } else {
                users = userRepository.findByCreatedAtBetween(startDate, endDate, pageable);
            }
        } else {
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
        }

        return users.map(this::mapToUserResponse);
    }

    @Override
    @AdminActionLog(action = "UPDATE_USER_STATUS",
            targetType = "User")
    public boolean updateUserStatus(UpdateUserStatusRequest request, User admin) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + request.getUserId()));
        if (RoleName.ADMIN.equals(user.getRole().getRoleName())) {
            throw new UnauthorizedActionException("Không thể thay đổi trạng thái của Admin");
        }
        if (user.getStatus() == request.getStatus()) {
            return false;
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
        sendStatusEmail(user, request.getStatus(), request.getReason(), admin);
        return true;
    }

    @Override
    public void verifyAndCreateDoctor(VerifyPendingDoctorRequest request, User admin) {
        String otp = request.getOtp();
        String adminEmail = admin.getEmail();

        PendingDoctorData pending = PendingDoctorStore.getPendingByAdminEmail(adminEmail);
        if (pending == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu tạo bác sĩ hoặc đã hết hạn.");
        }

        boolean isOtpValid = OtpUtil.verifyOtp(adminEmail, otp);
        if (!isOtpValid) {
            throw new InvalidOtpException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }

        OtpUtil.removeOtp(adminEmail);
        PendingDoctorStore.removePending(pending.getRequestId());

        if (userRepository.existsByEmail(pending.getEmail())) {
            throw new DuplicateResourceException("Tài khoản email đã tồn tại: " + pending.getEmail());
        }
        if (userRepository.findByUserName(pending.getUserName()).isPresent()) {
            throw new DuplicateResourceException("Tên người dùng đã tồn tại: " + pending.getUserName());
        }
        if (userRepository.existsByPhoneNumber(pending.getPhoneNumber())) {
            throw new DuplicateResourceException("Số điện thoại đã được sử dụng: " + pending.getPhoneNumber());
        }
        if (pending.getNationalId() != null && userRepository.existsByNationalID(pending.getNationalId())) {
            throw new DuplicateResourceException("CCCD đã được sử dụng: " + pending.getNationalId());
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
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy role bác sĩ"));
        user.setRole(doctorRole);
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        User savedUser = userRepository.save(user);

        SystemLog log = SystemLog.builder()
                .action("CREATE_DOCTOR")
                .description("ADMIN: Tạo tài khoản bác sĩ: " + savedUser.getFullName() + " (" + savedUser.getUserName() + ")")
                .targetType("User")
                .targetId(savedUser.getUserId())
                .user(admin)
                .performedAt(LocalDateTime.now())
                .build();
        systemLogRepository.save(log);

        emailService.sendEmail(
                savedUser.getEmail(),
                "Tài khoản bác sĩ đã được tạo",
                EmailUtil.buildCreateDoctorAccountTemplate(savedUser.getFullName(), savedUser.getUserName(), rawPassword)
        );
    }

    @Override
    public InitiateCreateDoctorResponse initiateCreateDoctor(InitiateCreateDoctorRequest request, User admin) {
        String adminEmail = admin.getEmail();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Tài khoản email đã tồn tại: " + request.getEmail());
        }
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new DuplicateResourceException("Tên người dùng đã tồn tại: " + request.getUserName());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Số điện thoại đã được sử dụng: " + request.getPhoneNumber());
        }
        if (request.getNationalId() != null && userRepository.existsByNationalID(request.getNationalId())) {
            throw new DuplicateResourceException("CCCD đã được sử dụng: " + request.getNationalId());
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
        PendingDoctorStore.savePending(adminEmail, pending);

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(adminEmail, otp);

        emailService.sendEmail(
                adminEmail,
                "Mã xác thực OTP tạo tài khoản Bác sĩ",
                EmailUtil.buildCreateDoctorOtpForAdmin(admin.getFullName(), otp)
        );

        return InitiateCreateDoctorResponse.builder()
                .requestId(requestId)
                .message("OTP đã được gửi đến email " + adminEmail)
                .build();
    }

    @Override
    public DashboardStatsResponse getDashboardStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.countUsersWithDateFilter(start, end))
                .totalDoctors(userRepository.countUsersByRoleWithDateFilter(RoleName.DOCTOR, start, end))
                .totalPatients(userRepository.countUsersByRoleWithDateFilter(RoleName.PATIENT, start, end))
                .blockedUsers(userRepository.countUsersByStatusWithDateFilter(UserStatus.BANNED, start, end))
                .totalDiagnosisSessions(diagnosisSessionRepository.countSessionsWithDateFilter(start, end))
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
    public ChartStatsResponse getChartStats(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null && endDate == null) {
                LocalDate now = LocalDate.now();
                LocalDate currentMonthStart = now.withDayOfMonth(1);
                startDate = currentMonthStart.minusMonths(5);
                endDate = currentMonthStart;
            }

            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

            List<Object[]> userResults = userRepository.getMonthlyUserRegistrations(start, end);
            List<MonthlyStats> userRegistrations = new ArrayList<>();
            if (userResults != null && !userResults.isEmpty()) {
                int maxSize = Math.min(userResults.size(), 6);
                userRegistrations = userResults.stream()
                        .limit(maxSize)
                        .map(row -> MonthlyStats.builder()
                                .month(row[0].toString())
                                .count(((Number) row[1]).longValue())
                                .build())
                        .collect(Collectors.toList());
            }

            List<Object[]> sessionResults = diagnosisSessionRepository.getMonthlyDiagnosisSessions(start, end);
            List<MonthlyStats> diagnosisSessions = new ArrayList<>();
            if (sessionResults != null && !sessionResults.isEmpty()) {
                int maxSize = Math.min(sessionResults.size(), 6);
                diagnosisSessions = sessionResults.stream()
                        .limit(maxSize)
                        .map(row -> MonthlyStats.builder()
                                .month(row[0].toString())
                                .count(((Number) row[1]).longValue())
                                .build())
                        .collect(Collectors.toList());
            }

            return ChartStatsResponse.builder()
                    .userRegistrations(userRegistrations)
                    .diagnosisSessions(diagnosisSessions)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi lấy chart stats: ", e);
            return ChartStatsResponse.builder()
                    .userRegistrations(new ArrayList<>())
                    .diagnosisSessions(new ArrayList<>())
                    .build();
        }
    }

    @Override
    public GlobalSearchResponse searchGlobal(String keyword) {
        Pageable limit = PageRequest.of(0, 5);

        if (keyword == null || keyword.isEmpty()) {
            return GlobalSearchResponse.builder()
                    .users(new ArrayList<>())
                    .logs(new ArrayList<>())
                    .blockedIPs(new ArrayList<>())
                    .build();
        }

        List<User> users = userRepository.searchUsersByKeyword(keyword, limit);
        List<SystemLog> logs = systemLogRepository.searchLogsByKeyword(keyword, limit);
        List<BlockedIP> blockedIPs = blockedIPRepository.searchByKeyword(keyword, limit);

        List<UserSearchDTO> userDTOs = users.stream()
                .map(this::mapToUserSearchDTO)
                .collect(Collectors.toList());

        List<LogSearchDTO> logDTOs = logs.stream()
                .map(this::mapToLogSearchDTO)
                .collect(Collectors.toList());

        List<SecuritySearchDTO> searchDTOs = blockedIPs.stream()
                .map(this::mapToSecuritySearchDTO)
                .collect(Collectors.toList());

        return GlobalSearchResponse.builder()
                .users(userDTOs)
                .logs(logDTOs)
                .blockedIPs(searchDTOs)
                .build();
    }

    @Override
    public Map<String, Object> resendOtp(String adminEmail) {
        Map<String, Object> response = new HashMap<>();
        try {
            PendingDoctorData pending = PendingDoctorStore.getPendingByAdminEmail(adminEmail);
            if (pending == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy yêu cầu tạo bác sĩ đang chờ xử lý.");
                return response;
            }
            String otp = OtpUtil.generateOtp();
            OtpUtil.saveOtp(adminEmail, otp);

            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new UserNotFoundException("Không tìm thấy admin với email: " + adminEmail));

            emailService.sendEmail(
                    adminEmail,
                    "Mã xác thực OTP mới - Tạo tài khoản Bác sĩ",
                    EmailUtil.buildCreateDoctorOtpForAdmin(admin.getFullName(), otp)
            );
            response.put("success", true);
            response.put("message", "Đã gửi lại mã OTP mới. Vui lòng kiểm tra email.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi gửi lại OTP: " + e.getMessage());
        }
        return response;
    }

    @Override
    public User getAdminUser() {
        return userRepository.findFirstByRoleRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin trong hệ thống"));
    }

    private List<MonthlyStats> getMonthlyUserRegistrations(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

            List<Object[]> results = userRepository.getMonthlyUserRegistrations(start, end);
            return mapToMonthlyStats(results);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<MonthlyStats> getMonthlyDiagnosisSessions(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

            List<Object[]> results = diagnosisSessionRepository.getMonthlyDiagnosisSessions(start, end);
            return mapToMonthlyStats(results);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private UserSearchDTO mapToUserSearchDTO(User user) {
        return UserSearchDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roleName(user.getRole().getRoleName().name())
                .status(user.getStatus().name())
                .build();
    }

    private LogSearchDTO mapToLogSearchDTO(SystemLog log) {
        return LogSearchDTO.builder()
                .logId(log.getLogId())
                .action(log.getAction())
                .actionDisplay(mapActionToVietnamese(log.getAction()))
                .description(log.getDescription())
                .username(log.getUser().getUserName())
                .performedAt(log.getPerformedAt())
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

    private String mapActionToVietnamese(String action) {
        switch (action) {
            case "LOGIN": return "Đăng nhập";
            case "LOGOUT": return "Đăng xuất";
            case "BAN_USER": return "Khóa User";
            case "UNBAN_USER": return "Mở khóa";
            case "CREATE_DOCTOR": return "Tạo bác sĩ";
            case "UPDATE_USER_STATUS": return "Đổi trạng thái";
            case "BLOCK_IP": return "Chặn IP";
            case "UNBLOCK_IP": return "Mở khóa IP";
            case "PATIENT_NOTIFICATION": return "Thông báo bệnh nhân";
            case "CREATE_FINAL_DIAGNOSIS": return "Tạo chẩn đoán cuối";
            case "CREATE": return "Tạo phiên khám";
            case "UPDATE_SESSION_STATUS": return "Cập nhật trạng thái ca chẩn đoán";
            case "UPDATE_SESSION_SHARE": return "Cập nhật trạng thái công bố ca chẩn đoán";
            case "UPDATE_CLINICAL_SYMPTOMS": return "Cập nhật triệu chứng lâm sàng";
            case "BLOCKED_IP": return "Chặn IP";
            case "UNBLOCKED_IP": return "Mở khóa IP";
            default: return action;
        }
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

    private SecuritySearchDTO mapToSecuritySearchDTO(BlockedIP blockedIp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return SecuritySearchDTO.builder()
                .ipAddress(blockedIp.getIpAddress())
                .reason(blockedIp.getReason())
                .blockedAt(blockedIp.getCreatedAt() != null ? blockedIp.getCreatedAt().format(formatter) : "")
                .createdBy(blockedIp.getCreatedBy())
                .build();
    }

    // Helper method để lọc 12 tháng gần nhất
    private List<MonthlyStats> filterLast12Months(List<MonthlyStats> data, LocalDate fromDate) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        List<MonthlyStats> filtered = new ArrayList<>();
        for (MonthlyStats item : data) {
            try {
                String monthStr = item.getMonth();
                if (monthStr != null && monthStr.contains("/")) {
                    String[] parts = monthStr.split("/");
                    if (parts.length == 2) {
                        int month = Integer.parseInt(parts[0]);
                        int year = Integer.parseInt(parts[1]);
                        LocalDate itemDate = LocalDate.of(year, month, 1);
                        if (itemDate.isAfter(fromDate) || itemDate.isEqual(fromDate)) {
                            filtered.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                // Bỏ qua item không parse được
            }
        }
        return filtered;
    }

    private void sendStatusEmail(User user, UserStatus userStatus, String reason, User admin) {
        switch (userStatus) {
            case BANNED:
                emailService.sendEmail(
                        user.getEmail(),
                        "Tài khoản của bạn đã bị khoá",
                        EmailUtil.buildBanAccountTemplate(user.getFullName(), reason));
                break;
            case ACTIVE:
                emailService.sendEmail(
                        user.getEmail(),
                        "Tài khoản của bạn đã được mở khoá",
                        EmailUtil.buildUnbanAccountTemplate(user.getFullName(), reason));
                break;
            case INACTIVE:
                emailService.sendEmail(
                        user.getEmail(),
                        "Tài khoản của bạn đã được mở khoá",
                        EmailUtil.buildInactiveAccountTemplate(user.getFullName(), reason));
                break;
            default:
                throw new IllegalArgumentException("Unsupported user status: " + userStatus);
        }
    }
}