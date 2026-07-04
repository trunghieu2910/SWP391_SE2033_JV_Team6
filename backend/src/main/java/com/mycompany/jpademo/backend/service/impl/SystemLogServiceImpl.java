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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemLogServiceImpl implements SystemLogService {
    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    @Override
    public Page<SystemLogResponse> getLogs(String action, String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        String cleanAction = (action == null || action.isBlank()) ? null : action;
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;
        Page<SystemLog> systemLogs = systemLogRepository.filterLogs(cleanAction, cleanKeyword, startDate, endDate, pageable);
        return systemLogs.map(this::mapToSystemLogRespone);
    }

    @Override
    public void logActivity(String targetType, Integer targetId, String action, String description) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
                return;
            }
            User currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();

            SystemLog systemLog = SystemLog.builder()
                    .action(action)
                    .targetType(targetType != null ? targetType : "SYSTEM")
                    .targetId(targetId != null ? targetId : currentUser.getUserId())
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
