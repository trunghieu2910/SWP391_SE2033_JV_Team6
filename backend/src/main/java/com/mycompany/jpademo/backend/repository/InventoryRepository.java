package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    @Query("SELECT i FROM Inventory i WHERE " +
           "(:keyword IS NULL OR LOWER(i.batch.drug.drugName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.batch.batchNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:unit IS NULL OR LOWER(i.batch.drug.baseUnit.unitName) LIKE LOWER(CONCAT('%', :unit, '%')))")
    Page<Inventory> searchInventory(@Param("keyword") String keyword, @Param("unit") String unit, Pageable pageable);

    Optional<Inventory> findByBatch_BatchId(Integer batchId);

    // Tìm inventory theo batchId (alias cho findByBatch_BatchId)
    default Optional<Inventory> findByBatchId(Integer batchId) {
        return findByBatch_BatchId(batchId);
    }

    @Query("SELECT i FROM Inventory i WHERE i.batch.drug.drugId = :drugId")
    List<Inventory> findByDrugId(@Param("drugId") Integer drugId);

    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock < 50 AND i.batch.status = com.mycompany.jpademo.backend.enums.BatchStatus.ACTIVE ORDER BY i.batch.expiryDate ASC")
    Page<Inventory> findLowStockInventory(Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.batch.expiryDate <= :expiryThreshold AND i.batch.status IN (com.mycompany.jpademo.backend.enums.BatchStatus.ACTIVE, com.mycompany.jpademo.backend.enums.BatchStatus.EXPIRED) AND i.quantityInStock > 0")
    List<Inventory> findExpiringInventory(@Param("expiryThreshold") java.time.LocalDate expiryThreshold);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityInStock < 50 AND i.batch.status = com.mycompany.jpademo.backend.enums.BatchStatus.ACTIVE")
    long countLowStock();

    // Tìm tất cả inventory còn hàng cho một loại thuốc (dùng khi xuất thuốc)
    @Query("SELECT i FROM Inventory i WHERE i.batch.drug.drugId = :drugId " +
           "AND i.quantityInStock > 0 " +
           "AND i.batch.expiryDate >= CURRENT_DATE " +
           "AND (i.batch.drug.status IS NULL OR i.batch.drug.status != com.mycompany.jpademo.backend.enums.DrugStatus.INACTIVE) " +
           "AND (i.batch.status IS NULL OR i.batch.status != com.mycompany.jpademo.backend.enums.BatchStatus.INACTIVE) " +
           "ORDER BY i.batch.expiryDate ASC")
    List<Inventory> findActiveBatchesByDrugId(@Param("drugId") Integer drugId);
}
