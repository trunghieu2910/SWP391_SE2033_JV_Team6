package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    List<User> findByRoleRoleNameAndStatus(String roleName, UserStatus status);
}
