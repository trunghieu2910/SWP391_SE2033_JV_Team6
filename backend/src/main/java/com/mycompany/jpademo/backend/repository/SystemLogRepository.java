package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT l FROM SystemLog l WHERE " +
            "(:userId IS NULL OR l.user.userId = :userId) AND " +
            "(:action IS NULL OR l.action = :action) AND " +
            "(:keyword IS NULL OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(CAST(:startDate AS timestamp) IS NULL OR l.performedAt >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR l.performedAt <= :endDate)")
    Page<SystemLog> filterLogs(
            @Param("userId") Integer userId,
            @Param("action") String action,
            @Param("keyword") String keyword,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT l FROM SystemLog l WHERE " +
            "LOWER(l.action) LIKE LOWER(:keyword) OR " +
            "LOWER(l.description) LIKE LOWER(:keyword) OR " +
            "LOWER(l.user.userName) LIKE LOWER(:keyword)")
    List<SystemLog> searchLogsByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
