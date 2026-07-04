package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {
    Page<SystemLog> findByUserUserId(Integer userId, Pageable pageable);

    Page<SystemLog> findByAction(String action, Pageable pageable);

    Page<SystemLog> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);

    Page<SystemLog> findByUserUserIdAndAction(
            Integer userId, String action, Pageable pageable);

    Page<SystemLog> findByUserUserIdAndDescriptionContainingIgnoreCase(
            Integer userId, String keyword, Pageable pageable);

    Page<SystemLog> findByActionAndDescriptionContainingIgnoreCase(
            String action, String keyword, Pageable pageable);

    Page<SystemLog> findByUserUserIdAndActionAndDescriptionContainingIgnoreCase(
            Integer userId, String action, String keyword, Pageable pageable);

    List<SystemLog> findTop10ByUser_UserIdOrderByPerformedAtDesc(Integer userId);

    @Query(value = "SELECT * FROM SystemLog l WHERE " +
            "(:action IS NULL OR l.action = :action) AND " +
            "(:keyword IS NULL OR " +
            "l.action COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
            "l.description COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
            "l.targetType COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
            "CAST(l.targetID AS NVARCHAR) LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:startDate IS NULL OR l.performedAt >= :startDate) AND " +
            "(:endDate IS NULL OR l.performedAt <= :endDate)",
            countQuery = "SELECT COUNT(*) FROM SystemLog l WHERE " +
                    "(:action IS NULL OR l.action = :action) AND " +
                    "(:keyword IS NULL OR " +
                    "l.action COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
                    "l.description COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
                    "l.targetType COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
                    "CAST(l.targetID AS NVARCHAR) LIKE CONCAT('%', :keyword, '%')) AND " +
                    "(:startDate IS NULL OR l.performedAt >= :startDate) AND " +
                    "(:endDate IS NULL OR l.performedAt <= :endDate)",
            nativeQuery = true)
    Page<SystemLog> filterLogs(
            @Param("action") String action,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT l FROM SystemLog l WHERE " +
            "LOWER(l.action) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.user.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SystemLog> searchLogsByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
