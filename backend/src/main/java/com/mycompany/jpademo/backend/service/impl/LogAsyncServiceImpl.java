package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.entity.RequestLog;
import com.mycompany.jpademo.backend.repository.RequestLogRepository;
import com.mycompany.jpademo.backend.service.interfaces.LogAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogAsyncServiceImpl implements LogAsyncService {
    private final RequestLogRepository requestLogRepository;

    @Override
    @Async
    public void saveLogAsync(String ip, String uri, String method, String userAgent) {
        RequestLog requestLog = RequestLog.builder()
                .ipAddress(ip)
                .uri(uri)
                .method(method)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();
        requestLogRepository.save(requestLog);
    }
}
