package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemLogServiceImpl implements SystemLogService {
    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    @Override
    public Page<SystemLogResponse> getLogs(Integer userId, String action, String keyword, Pageable pageable) {
        Page<SystemLog> systemLogs;
        boolean hasUserId = userId != null;
        boolean hasAction = action != null && !action.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasAction && hasUserId && hasKeyword) {
            systemLogs = systemLogRepository.findByUserUserIdAndActionAndDescriptionContainingIgnoreCase(
                    userId, action, keyword, pageable);
        } else if (hasAction && hasUserId) {
            systemLogs = systemLogRepository.findByUserUserIdAndAction(
                    userId, action, pageable);
        } else if (hasAction && hasKeyword) {
            systemLogs = systemLogRepository.findByActionAndDescriptionContainingIgnoreCase(
                    action, keyword, pageable);
        } else if (hasKeyword && hasUserId) {
            systemLogs = systemLogRepository.findByUserUserIdAndDescriptionContainingIgnoreCase(
                    userId, keyword, pageable);
        } else if (hasAction) {
            systemLogs = systemLogRepository.findByAction(action, pageable);
        } else if (hasKeyword) {
            systemLogs = systemLogRepository.findByDescriptionContainingIgnoreCase(keyword, pageable);
        } else if (hasUserId) {
            systemLogs = systemLogRepository.findByUserUserId(userId, pageable);
        } else {
            systemLogs = systemLogRepository.findAll(pageable);
        }
        return systemLogs.map(this::mapToSystemLogRespone);
    }

    @Override
    public void logActivity(String targetType, Integer targetId, String action, String description) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;

            // Xử lý linh hoạt: Chỉ bắt thông tin User nếu SecurityContext vẫn còn hiệu lực (Ví dụ: Các hàm lấy dữ liệu bình thường)
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();
            } else {
                // ĐẶC BIỆT DÀNH CHO LOGOUT:
                // SecurityContext đã bị xóa, nhưng AOP đã kịp truyền targetId sang.
                // Ta lấy targetId đó để móc lại User từ Database.
                if (targetId != null && targetId > 0) {
                    currentUser = userRepository.findById(targetId).orElse(null);
                }
            }

            // Gán targetId an toàn (Nếu không truyền vào, lấy từ currentUser. Nếu vẫn không có, cho bằng 0)
            Integer safeTargetId = targetId;
            if (safeTargetId == null) {
                safeTargetId = (currentUser != null) ? currentUser.getUserId() : 0;
            }

            SystemLog systemLog = SystemLog.builder()
                    .action(action)
                    .targetType(targetType != null ? targetType : "SYSTEM")
                    .targetId(safeTargetId)
                    .description(description)
                    .user(currentUser)
                    .build();

            systemLogRepository.save(systemLog);
        } catch (Exception e) {
            log.warn("Không thể ghi log hoạt động: {}", e.getMessage());
        }
    }

    private SystemLogResponse mapToSystemLogRespone(SystemLog systemLog) {
        return SystemLogResponse.builder()
                .logId(systemLog.getLogId())
                .action(systemLog.getAction())
                .targetId(systemLog.getTargetId())
                .targetType(systemLog.getTargetType())
                .description(systemLog.getDescription())
                .performedAt(systemLog.getPerformedAt())
                .build();
    }
}
