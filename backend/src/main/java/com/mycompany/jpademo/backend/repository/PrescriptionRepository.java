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

    @Query("SELECT p FROM Prescription p WHERE p.status IN (com.mycompany.jpademo.backend.enums.PrescriptionStatus.PENDING, com.mycompany.jpademo.backend.enums.PrescriptionStatus.DISPENSED) AND p.session.status = com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus.COMPLETED ORDER BY p.prescriptionDate DESC")
    Page<Prescription> findAllDispensePrescriptionsPage(Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.session.status = com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus.COMPLETED " +
           "AND (:keyword IS NULL OR LOWER(p.patient.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR p.patient.user.nationalID LIKE CONCAT('%', :keyword, '%')) " +
           "AND (cast(:fromDate as timestamp) IS NULL OR p.prescriptionDate >= :fromDate) " +
           "AND (cast(:toDate as timestamp) IS NULL OR p.prescriptionDate <= :toDate) " +
           "AND (:status IS NULL " +
           "     OR (:status = 'pending' AND p.status = com.mycompany.jpademo.backend.enums.PrescriptionStatus.PENDING) " +
           "     OR (:status = 'completed' AND p.status = com.mycompany.jpademo.backend.enums.PrescriptionStatus.DISPENSED)) " +
           "ORDER BY p.prescriptionDate DESC")
    Page<Prescription> filterDispensePrescriptionsPage(
        @Param("keyword") String keyword,
        @Param("status") String status,
        @Param("fromDate") java.time.LocalDateTime fromDate,
        @Param("toDate") java.time.LocalDateTime toDate,
        Pageable pageable);
}
