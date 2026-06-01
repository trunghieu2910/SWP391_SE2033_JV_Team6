package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.UserStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    @Column(name = "userName", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "fullName", length = 100)
    private String fullName;

    @Column(name = "passwordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phoneNumber", length = 20, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private UserStatusEnum status;

    @Column(name = "lastChangePassTime")
    private LocalDateTime lastChangePassTime;

    @Column(name = "lastLoginTime")
    private LocalDateTime lastLogoutTime;

    @CreationTimestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "nationalID", length = 12)
    private String nationalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleID", nullable = false)
    private Role role;
}