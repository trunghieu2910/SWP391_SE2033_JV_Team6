package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateDrugRequest;
import com.mycompany.jpademo.backend.dto.request.DispenseDrugRequest;
import com.mycompany.jpademo.backend.dto.request.ImportDrugBatchRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.InventoryLog;
import com.mycompany.jpademo.backend.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PharmacistService {
    // Dashboard
    PharmacistDashboardDTO getDashboardStats();

    // Drug Management
    Page<DrugDTO> getDrugList(Pageable pageable);
    Page<DrugDTO> searchDrugs(String search, Pageable pageable);
    List<DrugDTO> getAllActiveDrugs();
    Page<DrugDTO> getDrugsByCategory(Integer subCategoryId, Pageable pageable);
    DrugDTO getDrugDetail(Integer drugId);
    DrugDTO createDrug(CreateDrugRequest request, Integer pharmacistId);
    DrugDTO updateDrug(Integer drugId, CreateDrugRequest request);
    void updateDrugStatus(Integer drugId, Byte status);
    String generateNextDrugCode();
    String generateNextBatchStt();
    List<Unit> getAllUnits();

    // Drug Batch Management
    Page<DrugBatchDTO> getDrugBatches(Pageable pageable);
    DrugBatchDTO createDrugBatch(ImportDrugBatchRequest request, Integer pharmacistId);
    DrugBatchDTO getDrugBatchDetail(Integer batchId);
    DrugBatchDTO updateDrugBatch(Integer batchId, ImportDrugBatchRequest request, Integer pharmacistId);
    List<DrugBatchDTO> getBatchesByDrug(Integer drugId);
    List<DrugBatchDTO> getExpiringBatches(LocalDate days);
    List<DrugBatchDTO> getExpiredBatches();

    // Inventory Management
    Page<InventoryDTO> getInventory(Pageable pageable);
    List<InventoryDTO> getLowStockInventory();
    List<InventoryDTO> getExpiringInventory();
    InventoryDTO getInventoryByBatch(Integer batchId);
    void adjustInventory(Integer batchId, Integer quantityChange, String reason, Integer pharmacistId);

    // Prescription & Dispensing
    Page<PrescriptionDetailDTO> getPendingPrescriptions(Pageable pageable);
    List<PrescriptionSummaryDTO> getPendingPrescriptionsSummary();
    List<PrescriptionDetailDTO> getPrescriptionDetailsByPrescriptionId(Integer prescriptionId);
    PrescriptionDetailDTO getPrescriptionDetail(Integer detailId);
    void dispenseDrug(DispenseDrugRequest request, Integer pharmacistId);
    Page<PrescriptionDetailDTO> getPrescriptionHistory(Pageable pageable);
    List<DrugBatchDTO> getActiveBatchesForDrug(Integer drugId);

    // Reports
    List<InventoryLog> getInventoryLog(LocalDate startDate, LocalDate endDate);
    Page<InventoryLog> getInventoryLog(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
