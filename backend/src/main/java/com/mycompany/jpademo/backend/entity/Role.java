package com.mycompany.jpademo.backend.entity;

import com.mycompany.jpademo.backend.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @Column(name = "roleID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Column(name = "roleName", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @OneToMany(mappedBy = "role")
    private List<User> users;
}
