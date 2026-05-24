package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.SystemLogRespone;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {
    private final SystemLogRepository systemLogRepository;

    @Override
    public List<SystemLogRespone> getAllLogs() {
        List<SystemLog> systemLog = systemLogRepository.findAll();
        return getSystemLogRespone(systemLog);
    }

    @Override
    public List<SystemLogRespone> getLogByUser(Integer userId) {
        List<SystemLog> systemLogs = systemLogRepository.findByUserUserId(userId);
        return getSystemLogRespone(systemLogs);
    }

    @Override
    public List<SystemLogRespone> searchLogs(String keyword) {
        List<SystemLog> systemLogs = new ArrayList<>();
        systemLogs.addAll(systemLogRepository.findByDescriptionContaining(keyword));
        systemLogs.addAll(systemLogRepository.findByTargetTypeContaining(keyword));
        return getSystemLogRespone(systemLogs);
    }

    @Override
    public List<SystemLogRespone> getLogByAction(String action) {
        List<SystemLog> systemLogs = systemLogRepository.findByAction(action);
        return getSystemLogRespone(systemLogs);
    }

    @NonNull
    private List<SystemLogRespone> getSystemLogRespone(List<SystemLog> systemLogs) {
        List<SystemLogRespone> respones = null;
        for (SystemLog systemLog: systemLogs) {
            SystemLogRespone respone = SystemLogRespone.builder()
                    .logID(systemLog.getLogId())
                    .action(systemLog.getAction())
                    .targetId(systemLog.getTargetId())
                    .targetType(systemLog.getTargetType())
                    .description(systemLog.getDescription())
                    .performedAt(systemLog.getPerformedAt())
                    .build();
            respones.add(respone);
        }
        return respones;
    }
}
