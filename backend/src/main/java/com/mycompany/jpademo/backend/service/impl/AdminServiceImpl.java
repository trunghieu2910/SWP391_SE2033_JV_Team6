package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.request.InitiateCreateStaffRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingStaffRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.*;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import com.mycompany.jpademo.backend.util.OtpUtil;
import com.mycompany.jpademo.backend.cache.PendingStaffData;
import com.mycompany.jpademo.backend.cache.PendingStaffStore;
import com.mycompany.jpademo.backend.util.SecureFileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: GiangLTHE194888
 * Task: Service implementation containing business logic for system administrators, managing user accounts, audits, dashboard data, and certificates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final SystemLogRepository systemLogRepository;

    private final RequestLogRepository requestLogRepository;

    private final DiagnosisSessionRepository diagnosisSessionRepository;

    private final EmailService emailService;

    private final BlockedIPRepository blockedIPRepository;

    private static final int SEARCH_MIN_KEYWORD_LENGTH = 2;

    /** Retrieves a paginated list of users based on search filters. */
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

    /** Updates the status of a specific user. */
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
            throw new BadRequestException("Không thể cập nhật vì người dùng đã ở trạng thái này.");
        }
        user.setStatus(request.getStatus());
        userRepository.save(user);
        sendStatusEmail(user, request.getStatus(), request.getReason(), admin);
        return true;
    }

    /** Verifies the OTP and finalizes doctor creation. */
    @Override
    @Transactional
    public void verifyAndCreateStaff(VerifyPendingStaffRequest request, User admin) {
        String otp = request.getOtp();
        String adminEmail = admin.getEmail();

        PendingStaffData pending = PendingStaffStore.getPending(request.getRequestId());
        if (pending == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu tạo tài khoản hoặc đã hết hạn.");
        }

        if (!pending.getAdminEmail().equals(adminEmail)) {
            throw new UnauthorizedActionException("Yêu cầu này không thuộc quyền quản lý của tài khoản của bạn.");
        }

        boolean isOtpValid = OtpUtil.verifyOtp(adminEmail, otp);
        if (!isOtpValid) {
            throw new InvalidOtpException();
        }

        OtpUtil.removeOtp(adminEmail);
        PendingStaffStore.removePending(pending.getRequestId());

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
        Role role = roleRepository.findByRoleName(RoleName.valueOf(pending.getRoleName()))
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy role: " + pending.getRoleName()));
        user.setRole(role);
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        User savedUser = userRepository.save(user);

        SystemLog log = SystemLog.builder()
                .action("CREATE_STAFF")
                .description("ADMIN: Tạo tài khoản: " + savedUser.getFullName() + " (" + savedUser.getUserName() + ")")
                .targetType("User")
                .targetId(savedUser.getUserId())
                .user(admin)
                .performedAt(LocalDateTime.now())
                .build();
        systemLogRepository.save(log);

        sendCreateStaffEmail(savedUser, savedUser.getRole().getRoleName());
    }

    /** Initiates the creation of a doctor account by sending an OTP. */
    @Override
    public InitiateCreateStaffResponse initiateCreateStaff(InitiateCreateStaffRequest request, User admin) {
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
        String certificateStoredFileName = null;
        if (request.getCertificateFile() != null && !request.getCertificateFile().isEmpty()) {
            try {
                String customName = request.getUserName() + "_" + System.currentTimeMillis();
                certificateStoredFileName = SecureFileUploadUtil.validateAndGenerateCustomFileName(request.getCertificateFile(), customName);
                
                Path root = Paths.get(System.getProperty("user.dir"));
                Path uploadDir;
                if (root.getFileName().toString().equals("backend")) {
                    uploadDir = root.resolve(Paths.get("uploads", "certificates"));
                } else {
                    uploadDir = root.resolve(Paths.get("uploads", "certificates"));
                }
                Files.createDirectories(uploadDir);

                Path target = SecureFileUploadUtil.resolveSafely(uploadDir, certificateStoredFileName);
                request.getCertificateFile().transferTo(target);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu file bằng cấp: " + e.getMessage());
            }
        }

        PendingStaffData pending = PendingStaffData.builder()
                .requestId(requestId)
                .userName(request.getUserName())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .nationalId(request.getNationalId())
                .certificateUrl(certificateStoredFileName)
                .roleName(request.getRoleName())
                .build();
        PendingStaffStore.savePending(adminEmail, pending);

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(adminEmail, otp);

        sendCreateStaffOtpEmail(admin, RoleName.valueOf(request.getRoleName()), otp);

        return InitiateCreateStaffResponse.builder()
                .requestId(requestId)
                .message("OTP đã được gửi đến email " + adminEmail)
                .build();
    }

    /** Retrieves page data for the admin dashboard. */
    @Override
    public DashboardPageResponse getDashboardPageData(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate resolvedStart = range[0];
        LocalDate resolvedEnd = range[1];

        try {
            DashboardStatsResponse stats = ensureStatsNotNull(getDashboardStats(resolvedStart, resolvedEnd));
            ChartStatsResponse charts = ensureChartsNotNull(getChartStats(resolvedStart, resolvedEnd));

            LocalDateTime startLogs = resolvedStart != null ? resolvedStart.atStartOfDay() : null;
            LocalDateTime endLogs = resolvedEnd != null ? resolvedEnd.atTime(java.time.LocalTime.MAX) : null;

            Page<SystemLog> recentLogsPage = systemLogRepository.filterLogs(
                    null, null, startLogs, endLogs,
                    PageRequest.of(0, 10, Sort.by("performedAt").descending())
            );
            List<SystemLog> logList = recentLogsPage != null
                    ? recentLogsPage.getContent()
                    : new ArrayList<>();

            return DashboardPageResponse.builder()
                    .stats(stats)
                    .charts(charts)
                    .recentLogs(mapToSystemLogResponse(logList))
                    .startDate(resolvedStart)
                    .endDate(resolvedEnd)
                    .build();

        } catch (Exception e) {
            log.error("ERROR loading dashboard: ", e);
            return DashboardPageResponse.builder()
                    .stats(ensureStatsNotNull(null))
                    .charts(ensureChartsNotNull(null))
                    .recentLogs(new ArrayList<>())
                    .startDate(resolvedStart)
                    .endDate(resolvedEnd)
                    .errorMessage("Không thể tải dữ liệu dashboard. Vui lòng thử lại.")
                    .build();
        }
    }

    /** Retrieves details of a specific user. */
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

    /** Retrieves chart statistical data for a given date range. */
    @Override
    public ChartStatsResponse getChartStats(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null && endDate == null) {
                LocalDate now = LocalDate.now();
                LocalDate currentMonthStart = now.withDayOfMonth(1);
                startDate = currentMonthStart.minusMonths(5);
                endDate = now;
            }

            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

            List<Object[]> userResults = userRepository.getMonthlyUserRegistrations(start, end);
            List<MonthlyStats> userRegistrations = new ArrayList<>();
            if (userResults != null && !userResults.isEmpty()) {
                userRegistrations = userResults.stream()
                        .map(row -> MonthlyStats.builder()
                                .month(row[0].toString())
                                .count(((Number) row[1]).longValue())
                                .build())
                        .collect(Collectors.toList());
                sortMonthlyStats(userRegistrations);
            }

            List<Object[]> requestResults = requestLogRepository.getMonthlyRequestTrend(start, end);
            List<MonthlyStats> requestTrends = new ArrayList<>();
            if (requestResults != null && !requestResults.isEmpty()) {
                requestTrends = requestResults.stream()
                        .map(row -> MonthlyStats.builder()
                                .month(row[0].toString())
                                .count(((Number) row[1]).longValue())
                                .build())
                        .collect(Collectors.toList());
                sortMonthlyStats(requestTrends);
            }

            return ChartStatsResponse.builder()
                    .userRegistrations(userRegistrations)
                    .requestTrends(requestTrends)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi lấy chart stats: ", e);
            return ChartStatsResponse.builder()
                    .userRegistrations(new ArrayList<>())
                    .requestTrends(new ArrayList<>())
                    .build();
        }
    }

    /** Performs a global search across different entities. */
    @Override
    public GlobalSearchResponse searchGlobal(String keyword) {
        Pageable limit = PageRequest.of(0, 5);

        String trimmedKeyword = keyword == null ? null : keyword.trim();
        if (trimmedKeyword == null || trimmedKeyword.length() < SEARCH_MIN_KEYWORD_LENGTH) {
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

    /** Resends the verification OTP code to the admin email. */
    @Override
    public Map<String, Object> resendOtp(String adminEmail) {
        Map<String, Object> response = new HashMap<>();
        try {
            PendingStaffData pending = PendingStaffStore.getPendingByAdminEmail(adminEmail);
            if (pending == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy yêu cầu tạo tài khoản đang chờ xử lý.");
                return response;
            }
            String otp = OtpUtil.generateOtp();
            OtpUtil.saveOtp(adminEmail, otp);

            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new UserNotFoundException("Không tìm thấy admin với email: " + adminEmail));

            sendCreateStaffOtpEmail(admin, RoleName.valueOf(pending.getRoleName()), otp);
            response.put("success", true);
            response.put("message", "Đã gửi lại mã OTP mới. Vui lòng kiểm tra email.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi gửi lại OTP: " + e.getMessage());
        }
        return response;
    }

    /** Retrieves the current admin user entity. */
    @Override
    public User getAdminUser() {
        return userRepository.findFirstByRoleRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin trong hệ thống"));
    }

    /** Retrieves a doctor's certificate resource and metadata. */
    @Override
    public CertificateFileResponse getStaffCertificate(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));

        if (RoleName.PATIENT.equals(user.getRole().getRoleName())) {
            throw new BadRequestException("Người dùng này không có chứng chỉ hành nghề.");
        }

        String storedFileName = user.getCertificateUrl();
        if (storedFileName == null) {
            throw new ResourceNotFoundException("Người dùng này chưa có file chứng chỉ.");
        }

        Path root = Paths.get(System.getProperty("user.dir"));
        Path certDir;
        if (root.getFileName().toString().equals("backend")) {
            certDir = root.resolve(Paths.get("uploads", "certificates")).normalize();
        } else {
            certDir = root.resolve(Paths.get("uploads", "certificates")).normalize();
        }
        Path filePath = certDir.resolve(storedFileName).normalize();

        if (!filePath.startsWith(certDir) || !Files.exists(filePath)) {
            throw new ResourceNotFoundException("File không tồn tại.");
        }

        Resource resource = new FileSystemResource(filePath.toFile());

        String ext = storedFileName.substring(storedFileName.lastIndexOf('.'));
        return CertificateFileResponse.builder()
                .resource(resource)
                .mediaType(resolveSafeMediaType(storedFileName))
                .displayName("certificate-" + userId + ext)
                .build();
    }

    /** Retrieves all available role names. */
    @Override
    public List<String> getRoleName() {
        List<RoleName> roleNames = List.of(RoleName.values());
        List<String> roles = new ArrayList<>();
        for (RoleName s: roleNames) {
            roles.add(String.valueOf(s));
        }
        return roles;
    }

    /** Retrieves all available user status names. */
    @Override
    public List<String> getUserStatus() {
        List<UserStatus> userStatuses = List.of(UserStatus.values());
        List<String> status = new ArrayList<>();
        for (UserStatus u: userStatuses) {
            status.add(String.valueOf(u));
        }
        return status;
    }

    /** Retrieves user registrations aggregated by month. */
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


    /** Maps a user entity to a user search DTO. */
    private UserSearchDTO mapToUserSearchDTO(User user) {
        return UserSearchDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roleName(user.getRole().getRoleName().name())
                .status(user.getStatus().name())
                .build();
    }

    /** Maps a system log entity to a log search DTO. */
    private LogSearchDTO mapToLogSearchDTO(SystemLog log) {
        return LogSearchDTO.builder()
                .logId(log.getLogId())
                .action(log.getAction())
                .actionDisplay(mapActionToVietnamese(log.getAction()))
                .description(log.getDescription())
                .username(log.getUser() != null ? log.getUser().getUserName() : "Hệ thống")
                .performedAt(log.getPerformedAt())
                .build();
    }

    /** Sorts the monthly statistics list in chronological order. */
    private void sortMonthlyStats(List<MonthlyStats> stats) {
        stats.sort((a, b) -> {
            try {
                String[] partsA = a.getMonth().split("/");
                String[] partsB = b.getMonth().split("/");
                int monthA = Integer.parseInt(partsA[0]);
                int yearA = Integer.parseInt(partsA[1]);
                int monthB = Integer.parseInt(partsB[0]);
                int yearB = Integer.parseInt(partsB[1]);
                if (yearA != yearB) {
                    return Integer.compare(yearA, yearB);
                }
                return Integer.compare(monthA, monthB);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    /** Maps database query rows to monthly stats DTO list. */
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

    /** Maps a user entity to a user response DTO. */
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

    /** Translates system log actions into Vietnamese display strings. */
    private String mapActionToVietnamese(String action) {
        switch (action) {
            // Nhóm Đăng nhập
            case "LOGIN":
            case "GOOGLE_LOGIN_FIRST_TIME":
            case "GOOGLE_LOGIN":
                return "Đăng nhập";

            // Nhóm Tạo
            case "CREATE_DOCTOR":
            case "CREATE_FINAL_DIAGNOSIS":
            case "CREATE":
            case "CREATE_LAB_RESULT":
                return "Tạo";

            // Nhóm Cập nhật
            case "UPDATE_SESSION_STATUS":
            case "UPDATE_SESSION_SHARE":
            case "UPDATE_CLINICAL_SYMPTOMS":
            case "UPDATE_PASSWORD":
            case "UPDATE_REMINDER":
                return "Cập nhật";

            // Nhóm Nhận kết quả
            case "LIS_RECEIVE":
            case "LIS_SIMULATE":
                return "Nhận kết quả";

            // Nhóm Xuất file pdf
            case "PATIENT_EXPORT_PDF_MEDICAL_RECORD":
            case "DOCTOR_EXPORT_PDF_MEDICAL_RECORD":
                return "Xuất file pdf";

            // Các case riêng lẻ
            case "LOGOUT":
                return "Đăng xuất";
            case "BAN_USER":
                return "Khóa User";
            case "UNBAN_USER":
                return "Mở khóa";
            case "UPDATE_USER_STATUS":
                return "Đổi trạng thái";
            case "BLOCKED_IP":
                return "Chặn IP";
            case "UNBLOCKED_IP":
                return "Mở khóa IP";
            case "PATIENT_NOTIFICATION": case "DOCTOR_NOTIFICATION":
                return "Thông báo";
            case "FORGOT_PASSWORD":
                return "Quên mật khẩu";
            case "VERIFY_OTP":
                return "Xác minh OTP";
            case "DELETE_LAB_RESULT":
                return "Xóa";
            case "REGISTER":
                return "Đăng kí";
            case "PATIENT_FILL_CLINLCAL_SYMPTOMS_FORM":
                return "Nhập";
            case "CREATE_REMINDER":
                return "Tạo nhắc nhở";
            case "VIEW_DIAGNOSIS_SESSIONS":
                return "Xem chẩn đoán";
            case "DELETE_REMINDER":
                return "Xoá nhắc nhở";
            case "REQUEST_MEDICAL_IMAGE":
                return "Yêu cầu";
            case "DELETE_MEDICAL_IMAGE":
                return "Xoá ảnh";
            case "RECEPTIONIST_CHANGE_PASSWORD":
                return "Đổi mật khẩu";
            case "RECEPTIONIST_CREATE_SESSION":
                return "Tạo ca khám";
            case "RECEPTIONIST_CREATE_PATIENT_ACCOUNT": case "CREATE_STAFF":
                return "Tạo tài khoản";
            case "PATIENT_SUBMIT": case "DOCTOR_SUBMIT":
                return "Nộp";
            default:
                return action;
        }
    }

    /** Maps a system log entity to a log response DTO. */
    private SystemLogResponse mapToLogResponse(SystemLog log) {
        return SystemLogResponse.builder()
                .logId(log.getLogId())
                .action(mapActionToVietnamese(log.getAction()))
                .description(log.getDescription())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .performedAt(log.getPerformedAt())
                .build();
    }

    /** Maps a blocked IP entity to a security search DTO. */
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
    /** Filters the monthly stats list to include only the last 12 months. */
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

    /** Sends an email notifying a user of their status update. */
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
            default:
                throw new IllegalArgumentException("Unsupported user status: " + userStatus);
        }
    }

    private void sendCreateStaffEmail(User savedUser, RoleName roleName) {
        switch (roleName) {
            case DOCTOR:
                emailService.sendEmail(savedUser.getEmail(),
                        "Tài khoản Bác sĩ đã được tạo",
                        EmailUtil.buildCreateDoctorAccountTemplate(
                                savedUser.getFullName(), savedUser.getUserName(), savedUser.getPasswordHash()));
                break;
            case ADMIN:
                emailService.sendEmail(savedUser.getEmail(),
                        "Tài khoản Quản trị viên đã được tạo",
                        EmailUtil.buildCreateAdminForAdmin(
                                savedUser.getFullName(), savedUser.getUserName(), savedUser.getPasswordHash()));
                break;
            case PHARMACIST:
                emailService.sendEmail(savedUser.getEmail(),
                        "Tài khoản Dược sĩ đã được tạo",
                        EmailUtil.buildCreatePharmacistForAdmin(
                                savedUser.getFullName(), savedUser.getUserName(), savedUser.getPasswordHash()));
                break;
            case RECEPTIONIST:
                emailService.sendEmail(savedUser.getEmail(),
                        "Tài khoản Lễ tân đã được tạo",
                        EmailUtil.buildCreateReceptionistForAdmin(
                                savedUser.getFullName(), savedUser.getUserName(), savedUser.getPasswordHash()));
                break;
            case ULTRASOUND_DOCTOR:
                emailService.sendEmail(savedUser.getEmail(),
                        "Tài khoản Bác sĩ siêu âm đã được tạo",
                        EmailUtil.buildCreateUltrasoundDoctorForAdmin(
                                savedUser.getFullName(), savedUser.getUserName(), savedUser.getPasswordHash()));
                break;
            default:
                throw new IllegalArgumentException("Không hỗ trợ gửi email cho người dùng với role " + savedUser.getRole().getRoleName());
        }
    }

    private void sendCreateStaffOtpEmail(User admin, RoleName roleName, String otp) {
        switch (roleName) {
            case DOCTOR:
                emailService.sendEmail(admin.getEmail(),
                        "Mã xác thực OTP tạo tài khoản Bác sĩ",
                        EmailUtil.buildCreateDoctorOtpForAdmin(
                                admin.getFullName(), otp));
                break;
            case ADMIN:
                emailService.sendEmail(admin.getEmail(),
                        "Mã xác thực OTP tạo tài khoản Quản trị viên",
                        EmailUtil.buildCreateAdminOtpForAdmin(
                                admin.getFullName(), otp));
                break;
            case PHARMACIST:
                emailService.sendEmail(admin.getEmail(),
                        "Mã xác thực OTP tạo tài khoản Dược sĩ",
                        EmailUtil.buildCreatePharmacistOtpForAdmin(
                                admin.getFullName(), otp));
                break;
            case RECEPTIONIST:
                emailService.sendEmail(admin.getEmail(),
                        "Mã xác thực OTP tạo tài khoản Lễ tân",
                        EmailUtil.buildCreateReceptionistOtpForAdmin(
                                admin.getFullName(), otp));
                break;
            case ULTRASOUND_DOCTOR:
                emailService.sendEmail(admin.getEmail(),
                        "Mã xác thực OTP tạo tài khoản Bác sĩ siêu âm",
                        EmailUtil.buildCreateUltrasoundDoctorOtpForAdmin(
                                admin.getFullName(), otp));
                break;
            default:
                throw new IllegalArgumentException("Không hỗ trợ gửi email cho người dùng với email " + admin.getEmail());
        }
    }

    /** Gathers and calculates dashboard summary counts. */
    private DashboardStatsResponse getDashboardStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.countUsersWithDateFilter(start, end))
                .totalDoctors(userRepository.countUsersByRoleWithDateFilter(RoleName.DOCTOR, start, end))
                .totalPatients(userRepository.countUsersByRoleWithDateFilter(RoleName.PATIENT, start, end))
                .totalPharmacist(userRepository.countUsersByRoleWithDateFilter(RoleName.PHARMACIST, start, end))
                .totalUltrasoundDoctor(userRepository.countUsersByRoleWithDateFilter(RoleName.ULTRASOUND_DOCTOR, start, end))
                .totalReceptionist(userRepository.countUsersByRoleWithDateFilter(RoleName.RECEPTIONIST, start, end))
                .blockedUsers(userRepository.countUsersByStatusWithDateFilter(UserStatus.BANNED, start, end))
                .totalDiagnosisSessions(diagnosisSessionRepository.countSessionsWithDateFilter(start, end))
                .build();
    }

    /** Maps system log list to system log response DTOs. */
    private List<SystemLogResponse> mapToSystemLogResponse(List<SystemLog> systemLog) {
        List<SystemLogResponse> list = new ArrayList<>();
        for (SystemLog s: systemLog) {
            SystemLogResponse systemLogResponse = new SystemLogResponse();
            systemLogResponse.setLogId(s.getLogId());
            systemLogResponse.setActionDisplay(mapActionToVietnamese(s.getAction()));
            systemLogResponse.setAction(s.getAction());
            systemLogResponse.setDescription(s.getDescription());
            systemLogResponse.setTargetId(s.getTargetId());
            systemLogResponse.setTargetType(s.getTargetType());
            systemLogResponse.setUserName(s.getUser() != null ? s.getUser().getUserName() : "Hệ thống");
            systemLogResponse.setPerformedAt(s.getPerformedAt());
            list.add(systemLogResponse);
        }
        return list;
    }

    /** Adjusts and validates starting and ending date ranges. */
    private LocalDate[] resolveDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return new LocalDate[]{null, null};
        } else if (startDate != null && endDate == null) {
            endDate = startDate.plusMonths(1);
        } else if (endDate != null && startDate == null) {
            startDate = endDate.minusMonths(6);
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        return new LocalDate[]{startDate, endDate};
    }

    /** Sanitizes dashboard stats, providing fallback default values if null. */
    private DashboardStatsResponse ensureStatsNotNull(DashboardStatsResponse stats) {
        if (stats != null) {
            return stats;
        }
        return DashboardStatsResponse.builder()
                .totalUsers(0L)
                .totalDoctors(0L)
                .totalPatients(0L)
                .blockedUsers(0L)
                .totalDiagnosisSessions(0L)
                .build();
    }

    /** Sanitizes chart stats, providing fallback default values if null. */
    private ChartStatsResponse ensureChartsNotNull(ChartStatsResponse charts) {
        List<MonthlyStats> userRegistrations = (charts != null && charts.getUserRegistrations() != null)
                ? charts.getUserRegistrations() : new ArrayList<>();
        List<MonthlyStats> requestTrends = (charts != null && charts.getRequestTrends() != null)
                ? charts.getRequestTrends() : new ArrayList<>();
        return ChartStatsResponse.builder()
                .userRegistrations(userRegistrations)
                .requestTrends(requestTrends)
                .build();
    }

    /** Identifies media type corresponding to certificate file extension. */
    private MediaType resolveSafeMediaType(String storedFileName) {
        String ext = storedFileName.substring(storedFileName.lastIndexOf('.')).toLowerCase();
        return switch (ext) {
            case ".png" -> MediaType.IMAGE_PNG;
            case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG;
            case ".pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM; // không xảy ra vì đã whitelist lúc upload
        };
    }
}