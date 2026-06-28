package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "RequestLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ipAddress", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "userAgent", length = 500)
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime  timestamp;
}
