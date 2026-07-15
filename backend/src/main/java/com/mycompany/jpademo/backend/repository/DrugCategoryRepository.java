package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DrugCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrugCategoryRepository extends JpaRepository<DrugCategory, Integer> {
    Optional<DrugCategory> findByCategoryName(String categoryName);
    
    @Query("SELECT c FROM DrugCategory c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<DrugCategory> findByIgnoreCase(@Param("search") String search, Pageable pageable);
}
