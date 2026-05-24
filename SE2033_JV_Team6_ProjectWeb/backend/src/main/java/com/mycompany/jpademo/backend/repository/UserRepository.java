package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByUsernameContaining(String keyword);

    List<User> findByEmailContaining(String keyword);

    List<User> findByRoleRoleNameAndStatus(String roleName, String status);
}
