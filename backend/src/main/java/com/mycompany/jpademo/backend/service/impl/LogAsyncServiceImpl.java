package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.internal.RequestLogEvent;
import com.mycompany.jpademo.backend.entity.RequestLog;
import com.mycompany.jpademo.backend.repository.RequestLogRepository;
import com.mycompany.jpademo.backend.service.interfaces.LogAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAsyncServiceImpl implements LogAsyncService {
    private final RequestLogBuffer requestLogBuffer;

    @Override
    public void saveLogAsync(String ip, String uri, String method, String userAgent) {
        boolean accepted = requestLogBuffer.offer(new RequestLogEvent(
           ip, uri, method, userAgent, LocalDateTime.now()
        ));
        if (!accepted) {
            log.warn("RequestLogBuffer đã đầy, b qua 1 log entry của URI: ", uri);
        }
    }
}
