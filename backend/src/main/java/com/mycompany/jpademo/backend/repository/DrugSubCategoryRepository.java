package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DrugSubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrugSubCategoryRepository extends JpaRepository<DrugSubCategory, Integer> {
    @Query("SELECT s FROM DrugSubCategory s WHERE s.category.categoryId = :categoryId")
    List<DrugSubCategory> findByCategoryId(@Param("categoryId") Integer categoryId);
    
    Optional<DrugSubCategory> findBySubCategoryName(String subCategoryName);
    
    @Query("SELECT s FROM DrugSubCategory s WHERE s.category.categoryId = :categoryId")
    Page<DrugSubCategory> findByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);
}
