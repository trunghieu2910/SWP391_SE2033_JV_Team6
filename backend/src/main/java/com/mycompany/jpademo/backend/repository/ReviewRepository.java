package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.dto.projection.DiseaseStatItem;
import com.mycompany.jpademo.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByDiagnosisSessionSessionId(Integer sessionId);

    @Query("""
    SELECT r.diseaseType.name AS diseaseName, COUNT(r) AS total
    FROM Review r
    WHERE r.reviewedAt BETWEEN :startDate AND :endDate
    GROUP BY r.diseaseType.name
    ORDER BY r.diseaseType.name ASC
    """)
    List<DiseaseStatItem> countByDiseaseTypeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}