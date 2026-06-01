package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByNationalID(String nationalID);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByUserName(String userName);
}
