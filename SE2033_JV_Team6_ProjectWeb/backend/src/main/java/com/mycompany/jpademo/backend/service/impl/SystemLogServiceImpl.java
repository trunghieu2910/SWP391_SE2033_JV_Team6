package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;

    @Override
    @Async
    public void logActivity(String targetType, Integer targetID, String action, String description) {
        User currentUser = null;

        // 1. Tự động lấy UserID từ SecurityContextHolder
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            currentUser = userDetails.getUser();
        }

        // 2. Lắp ráp đối tượng Log
        SystemLog logEntry = SystemLog.builder()
                .user(currentUser)
                .targetType(targetType)
                .targetId(targetID)
                .action(action)
                .description(description)
                .build();

        // 3. Ghi xuống Database
        systemLogRepository.save(logEntry);
    }
}
