package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.internal.RequestLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLogFlusher {
    private final RequestLogBuffer requestLogBuffer;
    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "INSERT INTO RequestLog (ipAddress, uri, method, userAgent, timestamp) VALUES (?, ?, ?, ?, ?)";

    @Scheduled(fixedRate = 3000)
    public void flush() {
        List<RequestLogEvent> batch = requestLogBuffer.drain(2000);
        if (batch.isEmpty()) {
            return;
        }

        try {
            jdbcTemplate.batchUpdate(INSERT_SQL, batch, batch.size(), (ps, event) -> {
               ps.setString(1, event.getIpAddress());
               ps.setString(2, event.getUri());
               ps.setString(3, event.getMethod());
               ps.setString(4, event.getUserAgent());
               ps.setTimestamp(5, Timestamp.valueOf(event.getTimestamp()));
            });
            log.info("Đã ghi request logs xuống DB.", batch.size());
        } catch (Exception e) {
            log.error("Lỗi khi thực hiện ghi batch {} logs xuống database", batch.size(), e);
        }
    }
}
