package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Integer> {
    Optional<Drug> findByDrugCode(String drugCode);
    Optional<Drug> findByDrugName(String drugName);
    
    @Query("SELECT d FROM Drug d WHERE d.subCategory.subCategoryId = :subCategoryId")
    Page<Drug> findBySubCategoryId(@Param("subCategoryId") Integer subCategoryId, Pageable pageable);
    
    Page<Drug> findByStatus(Byte status, Pageable pageable);
    java.util.List<Drug> findByStatus(Byte status);
    
    @Query("SELECT d FROM Drug d WHERE d.drugName LIKE %:search% OR d.drugCode LIKE %:search%")
    Page<Drug> searchDrugs(@Param("search") String search, Pageable pageable);

    @Query("SELECT d.drugCode FROM Drug d")
    java.util.List<String> findAllDrugCodes();
}
