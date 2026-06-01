package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.MedicalImageDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalImageDetailsRepository extends JpaRepository<MedicalImageDetails, Integer> {
    List<MedicalImageDetails> findByMedicalImageMedicalImageId(Integer medicalImageId);
}