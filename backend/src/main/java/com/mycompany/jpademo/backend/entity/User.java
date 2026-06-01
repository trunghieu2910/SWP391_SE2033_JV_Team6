package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    @Column(name = "userID")
    private Integer userId;
    
    @Column(name = "userName", nullable = false, unique = true, length = 50)
    private String userName;

    @Column(name = "fullName", length = 100)
    private String fullName;

    @Column(name = "passwordHash", nullable = false)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phoneNumber", length = 20, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private UserStatus status;

    @Column(name = "lastChangePassTime", nullable = false)
    private LocalDateTime lastChangePassTime;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "nationalID", length = 12)
    private String nationalID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleID", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user")
    private Patient patient;

    @OneToOne(mappedBy = "user")
    private Review review;

    @OneToMany(mappedBy = "user")
    private List<SystemLog> systemLogs;
}