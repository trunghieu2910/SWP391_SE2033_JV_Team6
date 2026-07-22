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

    /**
     * Aggregation query backing the Diagnosis Statistics page: groups all
     * Review records within [startDate, endDate] by disease type and counts
     * how many reviews fall into each group.
     * Returns a projection (DiseaseStatItem) rather than full Review
     * entities, since only the disease name and the count are needed —
     * avoids loading unnecessary entity data for a pure reporting query.
     * Results are ordered alphabetically by disease name for stable,
     * predictable rendering on the statistics page/chart.
     */

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