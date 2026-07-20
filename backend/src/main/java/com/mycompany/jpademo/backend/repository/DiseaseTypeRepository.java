package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DiseaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiseaseTypeRepository extends JpaRepository<DiseaseType, Integer> {

    Optional<DiseaseType> findByNameIgnoreCase(String name);
}
