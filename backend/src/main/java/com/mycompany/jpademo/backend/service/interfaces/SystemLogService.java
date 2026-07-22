package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Author: GiangLTHE194888
 * Task: Service interface defining operations for auditing and retrieving system activity logs.
 */
public interface SystemLogService {
    /** Retrieves a filtered, paginated list of system activity logs. */
    Page<SystemLogResponse> getLogs(String action, String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /** Records a new system activity log entry. */
    void logActivity(String targetType, Integer targetId, String action, String description);

    /** Retrieves detailed information for a specific system log entry. */
    SystemLogResponse getLogDetail(Integer logId);
}

