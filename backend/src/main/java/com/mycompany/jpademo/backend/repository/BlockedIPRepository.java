package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.BlockedIP;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlockedIPRepository extends JpaRepository<BlockedIP, String> {  // ← ID là String (ipAddress)
    boolean existsById(String ipAddress);

    @Query("SELECT b FROM BlockedIP b WHERE " +
            "LOWER(b.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.createdBy) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BlockedIP> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(b) FROM BlockedIP b WHERE " +
            "(:startDate IS NULL OR b.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR b.createdAt <= :endDate)")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM BlockedIP b WHERE " +
            "(:startDate IS NULL OR b.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR b.createdAt <= :endDate) " +
            "ORDER BY b.createdAt DESC")
    List<BlockedIP> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}