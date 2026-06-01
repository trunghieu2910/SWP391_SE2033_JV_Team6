package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String password, Pageable pageable);

    Page<User> findByRoleRoleNameAndStatus(RoleName roleName, UserStatus status,
                                           Pageable pageable);

    Page<User> findByRoleRoleName(RoleName roleName, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findAll(Pageable pageable);

    Long countByRoleRoleName(RoleName roleName);

    Long countByStatus(UserStatus status);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleName(
            String username, String email, RoleName roleName, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatus(
            String username, String email, UserStatus status, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleNameAndStatus(
            String username, String email, RoleName roleName, UserStatus status, Pageable pageable);

}
