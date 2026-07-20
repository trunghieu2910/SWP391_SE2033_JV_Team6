package com.mycompany.jpademo.backend.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RequestLogEvent {
    private final String ipAddress;
    private final String uri;
    private final String method;
    private final String userAgent;
    private final LocalDateTime timestamp;
}
