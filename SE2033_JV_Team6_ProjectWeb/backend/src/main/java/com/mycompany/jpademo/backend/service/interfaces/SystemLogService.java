package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SystemLogService {
    Page<SystemLogResponse> getLogs(Integer userId, String action, String keyword, Pageable pageable);

    void logActivity(String targetType, Integer targetId, String action, String description);
}
