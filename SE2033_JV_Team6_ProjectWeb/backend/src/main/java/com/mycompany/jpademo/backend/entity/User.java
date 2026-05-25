package com.mycompany.jpademo.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "[User]")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Integer userID;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "fullName", length = 100)
    private String fullName;

    @Column(name = "passwordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "certificate", length = 255)
    private String certificate;

    @Column(name = "phoneNumber", length = 20)
    private String phoneNumber;

    @Column(name = "status", length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "nationalID", unique = true, length = 12)
    private String nationalID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleID", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Patient patient;
}