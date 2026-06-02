package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
     Optional<Role> findByRoleName(RoleName roleName);
}
