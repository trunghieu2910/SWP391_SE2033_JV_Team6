package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.DrugBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrugBatchRepository extends JpaRepository<DrugBatch, Integer> {
    @Query("SELECT db FROM DrugBatch db WHERE db.batchNumber = :batchNumber AND db.drug.drugId = :drugId")
    Optional<DrugBatch> findByBatchNumberAndDrugId(@Param("batchNumber") String batchNumber, @Param("drugId") Integer drugId);
    
    @Query("SELECT db FROM DrugBatch db WHERE db.drug.drugId = :drugId")
    List<DrugBatch> findByDrugId(@Param("drugId") Integer drugId);
    
    List<DrugBatch> findByStatus(com.mycompany.jpademo.backend.enums.BatchStatus status);
    
    @Query("SELECT db FROM DrugBatch db WHERE db.expiryDate BETWEEN :startDate AND :endDate AND db.status IN (com.mycompany.jpademo.backend.enums.BatchStatus.ACTIVE, com.mycompany.jpademo.backend.enums.BatchStatus.EXPIRED) " +
           "AND EXISTS (SELECT i FROM Inventory i WHERE i.batch.batchId = db.batchId AND i.quantityInStock > 0)")
    List<DrugBatch> findExpiringBatches(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT db FROM DrugBatch db WHERE db.expiryDate < :today AND db.status = com.mycompany.jpademo.backend.enums.BatchStatus.ACTIVE")
    List<DrugBatch> findExpiredBatches(@Param("today") LocalDate today);

    @Query("SELECT db.batchNumber FROM DrugBatch db")
    java.util.List<String> findAllBatchNumbers();
}
