package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, Integer> {
    List<PrescriptionDetail> findByPrescription_PrescriptionId(Integer prescriptionId);

    // Alias de tuong thich voi code cu dung findByPrescriptionId
    default List<PrescriptionDetail> findByPrescriptionId(Integer prescriptionId) {
        return findByPrescription_PrescriptionId(prescriptionId);
    }

    @Query("SELECT pd FROM PrescriptionDetail pd WHERE pd.prescription.status = 0 AND (pd.quantityDispensed IS NULL OR pd.quantityDispensed = 0)")
    List<PrescriptionDetail> findPendingDispenses();

    @Query("SELECT pd FROM PrescriptionDetail pd WHERE pd.quantityDispensed > 0 ORDER BY pd.dispensedAt DESC")
    List<PrescriptionDetail> findDispensedDetails();

    @Query("SELECT pd FROM PrescriptionDetail pd WHERE pd.prescription.status IN (0, 1) ORDER BY pd.prescription.prescriptionDate DESC")
    List<PrescriptionDetail> findAllDispensePrescriptions();
}
