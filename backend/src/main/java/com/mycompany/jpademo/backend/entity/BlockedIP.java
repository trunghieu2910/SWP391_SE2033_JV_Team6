package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BlockedIP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedIP {
    @Id
    @Column(name = "ipAddress", length = 45)
    private String ipAddress;

    @Column(name = "reason")
    private String reason;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "createdBy", length = 100)
    private String createdBy;
}
