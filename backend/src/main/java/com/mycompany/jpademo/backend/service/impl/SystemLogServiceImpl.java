package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {
    private final SystemLogRepository systemLogRepository;

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
