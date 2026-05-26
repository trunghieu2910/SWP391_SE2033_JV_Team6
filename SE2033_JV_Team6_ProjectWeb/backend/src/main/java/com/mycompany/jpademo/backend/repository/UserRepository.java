package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmailOrUsernameOrPhoneNumber(
            String email,
            String username,
            String phoneNumber
    );

    Optional<User> findByEmail(String email);
}
