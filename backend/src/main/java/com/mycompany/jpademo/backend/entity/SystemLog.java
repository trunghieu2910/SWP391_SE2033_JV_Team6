package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SystemLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logID")
    private Integer logId;

    @Column(name = "targetType", nullable = false, length = 50)
    private String targetType;

    @Column(name = "targetID", nullable = false)
    private Integer targetId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "description", nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "performedAt", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = true)
    private User user;
}