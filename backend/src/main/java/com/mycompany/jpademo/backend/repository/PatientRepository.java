package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Optional<Patient> findByUser(User user);

    List<Patient> findByUserFullNameContainingIgnoreCaseOrUserNationalIDContainingIgnoreCase(String fullName, String nationalId);
}