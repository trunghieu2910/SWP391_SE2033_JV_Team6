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
    Optional<Inventory> findByBatch_BatchId(Integer batchId);

    // Tìm inventory theo batchId (alias cho findByBatch_BatchId)
    default Optional<Inventory> findByBatchId(Integer batchId) {
        return findByBatch_BatchId(batchId);
    }

    @Query("SELECT i FROM Inventory i WHERE i.batch.drug.drugId = :drugId")
    List<Inventory> findByDrugId(@Param("drugId") Integer drugId);

    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock < 50 AND i.status = 1 ORDER BY i.batch.expiryDate ASC")
    Page<Inventory> findLowStockInventory(Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.batch.expiryDate <= DATEADD(day, 7, CAST(GETDATE() as DATE)) AND i.status IN (1, 2) AND i.quantityInStock > 0")
    List<Inventory> findExpiringInventory();

    // Đếm số lượng inventory theo trạng thái
    long countByStatus(Byte status);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityInStock < 50 AND i.status = 1")
    long countLowStock();

    // Tìm tất cả inventory còn hàng cho một loại thuốc (dùng khi xuất thuốc)
    @Query("SELECT i FROM Inventory i WHERE i.batch.drug.drugId = :drugId " +
           "AND i.quantityInStock > 0 AND i.batch.status = 1 " +
           "AND i.batch.expiryDate >= CAST(GETDATE() as DATE) " +
           "ORDER BY i.batch.expiryDate ASC")
    List<Inventory> findActiveBatchesByDrugId(@Param("drugId") Integer drugId);
}
