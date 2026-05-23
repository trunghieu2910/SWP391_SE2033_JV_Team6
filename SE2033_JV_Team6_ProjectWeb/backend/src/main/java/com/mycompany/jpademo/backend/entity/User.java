package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "fullName", length = 100)
    private String fullName;

    @Column(name = "passwordHash", nullable = false)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "certificate")
    private String certificate;

    @Column(name = "phoneNumber", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "nationalID", length = 12)
    private String nationalID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleID", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user")
    private Patient patient;

    @OneToOne(mappedBy = "user")
    private Review review;

    @OneToMany(mappedBy = "user")
    private List<SystemLog> systemLogs;
}