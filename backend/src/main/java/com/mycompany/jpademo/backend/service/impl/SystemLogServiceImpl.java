package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Author: GiangLTHE194888
 * Task: Service implementation for managing system logs, tracking user activities, and fetching log detail data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemLogServiceImpl implements SystemLogService {
    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    /** Retrieves a filtered, paginated list of system activity logs. */
    @Override
    public Page<SystemLogResponse> getLogs(String action, String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        String cleanAction = (action == null || action.isBlank()) ? null : action;
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;
        Page<SystemLog> systemLogs = systemLogRepository.filterLogs(cleanAction, cleanKeyword, startDate, endDate, pageable);
        return systemLogs.map(this::mapToSystemLogResponse);
    }

    /** Records a new system activity log entry. */
    @Override
    public void logActivity(String targetType, Integer targetId, String action, String description) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();
            }

            if (currentUser == null && "Users".equals(targetType) && targetId != null) {
                currentUser = userRepository.findById(targetId).orElse(null);
            }

            SystemLog systemLog = SystemLog.builder()
                    .action(action)
                    .targetType(targetType != null ? targetType : "SYSTEM")
                    .targetId(targetId != null ? targetId
                            : (currentUser != null ? currentUser.getUserId() : 0))
                    .description(description)
                    .user(currentUser)
                    .build();

            systemLogRepository.save(systemLog);
        } catch (Exception e) {
            log.warn("Không thể ghi log hoạt động: {}", e.getMessage());
        }
    }

    /** Retrieves detailed information for a specific system log entry. */
    @Override
    public SystemLogResponse getLogDetail(Integer logId) {
        SystemLog systemLog = systemLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký với ID: " + logId));

        return SystemLogResponse.builder()
                .logId(systemLog.getLogId())
                .action(systemLog.getAction())
                .actionDisplay(mapActionToVietnamese(systemLog.getAction()))
                .targetType(systemLog.getTargetType())
                .targetId(systemLog.getTargetId())
                .description(systemLog.getDescription())
                .performedAt(systemLog.getPerformedAt())
                .userName(systemLog.getUser() != null ? systemLog.getUser().getUserName() : "Hệ thống")
                .build();
    }

    /** Translates system log actions into Vietnamese display strings. */
    private String mapActionToVietnamese(String action) {
        switch (action) {
            case "LOGIN": return "Đăng nhập";
            case "GOOGLE_LOGIN_FIRST_TIME": return "Đăng nhập";
            case "GOOGLE_LOGIN": return "Đăng nhập";
            case "LOGOUT": return "Đăng xuất";
            case "BAN_USER": return "Khóa User";
            case "UNBAN_USER": return "Mở khóa";
            case "CREATE_DOCTOR": return "Tạo";
            case "UPDATE_USER_STATUS": return "Đổi trạng thái";
            case "BLOCK_IP": return "Chặn IP";
            case "UNBLOCK_IP": return "Mở khóa IP";
            case "PATIENT_NOTIFICATION": return "Thông báo";
            case "CREATE_FINAL_DIAGNOSIS": return "Tạo";
            case "CREATE": return "Tạo";
            case "UPDATE_SESSION_STATUS": return "Cập nhật";
            case "UPDATE_SESSION_SHARE": return "Cập nhật";
            case "UPDATE_CLINICAL_SYMPTOMS": return "Cập nhật";
            case "FORGOT_PASSWORD": return "Quên mật khẩu";
            case "VERIFY_OTP": return "Xác minh OTP";
            case "UPDATE_PASSWORD": return "Cập nhật";
            case "CREATE_LAB_RESULT": return "Tạo";
            case "DELETE_LAB_RESULT": return "Xóa";
            case "LIS_RECEIVE": return "Nhận kết quả";
            case "LIS_SIMULATE": return "Nhận kết quả";
            default: return action;
        }
    }

    /** Maps a SystemLog entity to a SystemLogResponse DTO. */
    private SystemLogResponse mapToSystemLogResponse(SystemLog systemLog) {
        return SystemLogResponse.builder()
                .logId(systemLog.getLogId())
                .action(systemLog.getAction())
                .actionDisplay(mapActionToVietnamese(systemLog.getAction()))
                .targetId(systemLog.getTargetId())
                .targetType(systemLog.getTargetType())
                .description(systemLog.getDescription())
                .performedAt(systemLog.getPerformedAt())
                .userName(systemLog.getUser() != null ? systemLog.getUser().getUserName() : "Hệ thống")
                .build();
    }
}