package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
