package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {
    Optional<Prescription> findByPrescriptionCode(String prescriptionCode);
    List<Prescription> findByStatus(com.mycompany.jpademo.backend.enums.PrescriptionStatus status);
    Page<Prescription> findByStatus(com.mycompany.jpademo.backend.enums.PrescriptionStatus status, Pageable pageable);

    // FIX: Dung @Query thay vi method name de tranh loi 'No property id found for type Patient'
    @Query("SELECT p FROM Prescription p WHERE p.patient.patientId = :patientId")
    List<Prescription> findByPatientId(@Param("patientId") Integer patientId);

    @Query("SELECT p FROM Prescription p WHERE p.doctor.userId = :doctorId")
    List<Prescription> findByDoctorId(@Param("doctorId") Integer doctorId);

    @Query("SELECT p FROM Prescription p WHERE p.session.sessionId = :sessionId")
    Optional<Prescription> findBySessionSessionId(@Param("sessionId") Integer sessionId);
}
