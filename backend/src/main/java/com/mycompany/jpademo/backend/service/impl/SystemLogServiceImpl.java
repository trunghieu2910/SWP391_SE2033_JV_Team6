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
            case "BLOCK_IP":
                return "Chặn IP";
            case "UNBLOCK_IP":
                return "Mở khóa IP";
            case "PATIENT_NOTIFICATION":
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
            default:
                return action;
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