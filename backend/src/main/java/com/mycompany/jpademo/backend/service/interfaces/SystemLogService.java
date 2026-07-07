package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface SystemLogService {
    Page<SystemLogResponse> getLogs(String action, String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    void logActivity(String targetType, Integer targetId, String action, String description);

    SystemLogResponse getLogDetail(Integer logId);
}

