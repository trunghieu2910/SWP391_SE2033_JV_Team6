package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.InventoryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Integer> {
    // FIX: Dung @Query thay vi method name de tranh loi 'No property id found'
    @Query("SELECT il FROM InventoryLog il WHERE il.batch.batchId = :batchId")
    List<InventoryLog> findByBatchId(@Param("batchId") Integer batchId);

    Page<InventoryLog> findByActionType(String actionType, Pageable pageable);

    List<InventoryLog> findByPerformedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT il FROM InventoryLog il WHERE il.performedAt BETWEEN :startDate AND :endDate ORDER BY il.performedAt DESC")
    Page<InventoryLog> findByPerformedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT il FROM InventoryLog il WHERE il.user.userId = :userId AND il.actionType = :actionType")
    List<InventoryLog> findByUserIdAndActionType(@Param("userId") Integer userId, @Param("actionType") String actionType);
}
