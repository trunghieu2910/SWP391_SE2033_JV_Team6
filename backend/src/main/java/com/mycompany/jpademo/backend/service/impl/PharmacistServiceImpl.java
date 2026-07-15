package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateDrugRequest;
import com.mycompany.jpademo.backend.dto.request.DispenseDrugRequest;
import com.mycompany.jpademo.backend.dto.request.ImportDrugBatchRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.PharmacistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacistServiceImpl implements PharmacistService {
    private final DrugRepository drugRepository;
    private final DrugCategoryRepository drugCategoryRepository;
    private final DrugSubCategoryRepository drugSubCategoryRepository;
    private final DrugBatchRepository drugBatchRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final UnitConversionRepository unitConversionRepository;

    // ========================== DASHBOARD ==========================

    @Override
    public PharmacistDashboardDTO getDashboardStats() {
        int totalDrugs = (int) drugRepository.count();
        int totalBatches = (int) drugBatchRepository.count();

        List<Prescription> pendingPrescriptions = prescriptionRepository.findByStatus((byte) 0);
        int pendingCount = pendingPrescriptions.size();

        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        List<DrugBatch> expiringBatches = drugBatchRepository.findExpiringBatches(LocalDate.now(), sevenDaysFromNow);
        int expiringCount = expiringBatches.size();

        // FIX: Dung countByStatus truc tiep thay vi filter tu expiring list
        long lowStockCount = inventoryRepository.countByStatus((byte) 2);

        return PharmacistDashboardDTO.builder()
            .totalDrugs(totalDrugs)
            .pendingPrescriptions(pendingCount)
            .expiringDrugs(expiringCount)
            .lowStockDrugs((int) lowStockCount)
            .totalBatches(totalBatches)
            .lastUpdate(LocalDateTime.now())
            .build();
    }

    // ========================== DRUG MANAGEMENT ==========================

    @Override
    public Page<DrugDTO> getDrugList(Pageable pageable) {
        Page<Drug> drugs = drugRepository.findByStatus((byte) 1, pageable);
        return drugs.map(this::convertToDrugDTO);
    }

    @Override
    public Page<DrugDTO> searchDrugs(String search, Pageable pageable) {
        Page<Drug> drugs = drugRepository.searchActiveDrugs(search, pageable);
        return drugs.map(this::convertToDrugDTO);
    }

    @Override
    public Page<DrugDTO> getDrugsByCategory(Integer subCategoryId, Pageable pageable) {
        Page<Drug> drugs = drugRepository.findBySubCategoryId(subCategoryId, pageable);
        return drugs.map(this::convertToDrugDTO);
    }

    @Override
    public DrugDTO getDrugDetail(Integer drugId) {
        Drug drug = drugRepository.findById(drugId)
            .orElseThrow(() -> new RuntimeException("Drug not found: " + drugId));
        DrugDTO dto = convertToDrugDTO(drug);

        List<Inventory> inventories = inventoryRepository.findByDrugId(drugId);
        int totalQty = inventories.stream().mapToInt(Inventory::getQuantityInStock).sum();
        dto.setTotalQuantityInStock(totalQty);
        dto.setTotalBatches(drug.getBatches().size());

        return dto;
    }

    @Override
    @Transactional
    public DrugDTO createDrug(CreateDrugRequest request, Integer pharmacistId) {
        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        DrugSubCategory subCategory = drugSubCategoryRepository.findById(request.getSubCategoryId())
            .orElseThrow(() -> new RuntimeException("SubCategory not found: " + request.getSubCategoryId()));

        Drug drug = Drug.builder()
            .drugCode(request.getDrugCode())
            .drugName(request.getDrugName())
            .strength(request.getStrength())
            .strengthUnit(request.getStrengthUnit())
            .dosageForm(request.getDosageForm())
            .routeOfAdministration(request.getRouteOfAdministration())
            .subCategory(subCategory)
            .packaging(request.getPackaging())
            .manufacturer(request.getManufacturer())
            .countryOfOrigin(request.getCountryOfOrigin())
            .storageCondition(request.getStorageCondition())
            .shelfLifeMonths(request.getShelfLifeMonths())
            .notes(request.getNotes())
            .status((byte) 1)
            .createdByUser(pharmacist)
            .build();

        Drug savedDrug = drugRepository.save(drug);
        log.info("Drug created: {}", savedDrug.getDrugCode());
        return convertToDrugDTO(savedDrug);
    }

    @Override
    @Transactional
    public DrugDTO updateDrug(Integer drugId, CreateDrugRequest request) {
        Drug drug = drugRepository.findById(drugId)
            .orElseThrow(() -> new RuntimeException("Drug not found: " + drugId));

        drug.setDrugName(request.getDrugName());
        drug.setStrength(request.getStrength());
        drug.setStrengthUnit(request.getStrengthUnit());
        drug.setDosageForm(request.getDosageForm());
        drug.setRouteOfAdministration(request.getRouteOfAdministration());
        drug.setPackaging(request.getPackaging());
        drug.setManufacturer(request.getManufacturer());
        drug.setCountryOfOrigin(request.getCountryOfOrigin());
        drug.setStorageCondition(request.getStorageCondition());
        drug.setShelfLifeMonths(request.getShelfLifeMonths());
        drug.setNotes(request.getNotes());

        Drug updatedDrug = drugRepository.save(drug);
        log.info("Drug updated: {}", updatedDrug.getDrugCode());
        return convertToDrugDTO(updatedDrug);
    }

    @Override
    @Transactional
    public void updateDrugStatus(Integer drugId, Byte status) {
        Drug drug = drugRepository.findById(drugId)
            .orElseThrow(() -> new RuntimeException("Drug not found: " + drugId));
        drug.setStatus(status);
        drugRepository.save(drug);
        log.info("Drug status updated: {} to {}", drug.getDrugCode(), status);
    }

    // ========================== DRUG BATCH MANAGEMENT ==========================

    @Override
    public Page<DrugBatchDTO> getDrugBatches(Pageable pageable) {
        Page<DrugBatch> batches = drugBatchRepository.findAll(pageable);
        return batches.map(this::convertToDrugBatchDTO);
    }

    @Override
    @Transactional
    public DrugBatchDTO createDrugBatch(ImportDrugBatchRequest request, Integer pharmacistId) {
        Drug drug = drugRepository.findById(request.getDrugId())
            .orElseThrow(() -> new RuntimeException("Drug not found: " + request.getDrugId()));

        Unit unit = unitRepository.findById(request.getUnitId())
            .orElseThrow(() -> new RuntimeException("Unit not found: " + request.getUnitId()));

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        DrugBatch batch = DrugBatch.builder()
            .drug(drug)
            .batchNumber(request.getBatchNumber())
            .manufactureDate(request.getManufactureDate())
            .expiryDate(request.getExpiryDate())
            .unit(unit)
            .quantity(request.getQuantity())
            .importPrice(request.getImportPrice())
            .supplier(request.getSupplier())
            .importedByUser(pharmacist)
            .status((byte) 1)
            .notes(request.getNotes())
            .build();

        DrugBatch savedBatch = drugBatchRepository.save(batch);

        // FIX: Tinh so luong ton kho theo don vi nho nhat dua vao UnitConversion
        int quantityInSmallUnit = calculateSmallUnitQuantity(drug.getDrugId(), unit.getUnitId(), request.getQuantity());

        Inventory inventory = Inventory.builder()
            .batch(savedBatch)
            .quantityInStock(quantityInSmallUnit)
            .status((byte) 1)
            .build();
        inventoryRepository.save(inventory);

        InventoryLog logEntry = InventoryLog.builder()
            .batch(savedBatch)
            .user(pharmacist)
            .actionType("IMPORT")
            .quantityChange(quantityInSmallUnit)
            .quantityBefore(0)
            .quantityAfter(quantityInSmallUnit)
            .referenceId(savedBatch.getBatchId())
            .referenceType("BATCH")
            .notes(request.getNotes())
            .build();
        inventoryLogRepository.save(logEntry);

        log.info("Drug batch imported: {} - {} | {} {} => {} don vi nho",
            drug.getDrugCode(), request.getBatchNumber(),
            request.getQuantity(), unit.getUnitName(), quantityInSmallUnit);
        return convertToDrugBatchDTO(savedBatch);
    }

    /**
     * Tinh so luong quy doi sang don vi nho nhat dua vao bang UnitConversion.
     * Neu khong tim thay quy doi, dung nguyen so luong nhap.
     */
    private int calculateSmallUnitQuantity(Integer drugId, Integer largeUnitId, int quantity) {
        List<UnitConversion> conversions = unitConversionRepository.findByDrugId(drugId);
        Optional<UnitConversion> matchedConversion = conversions.stream()
            .filter(c -> c.getLargeUnit().getUnitId().equals(largeUnitId))
            .findFirst();

        if (matchedConversion.isPresent()) {
            int factor = matchedConversion.get().getConversionQuantity();
            log.debug("Quy doi: {} * {} = {} don vi nho", quantity, factor, quantity * factor);
            return quantity * factor;
        } else {
            log.warn("Khong tim thay quy doi don vi cho drugId={}, unitId={}. Dung nguyen so luong.", drugId, largeUnitId);
            return quantity;
        }
    }

    @Override
    public List<DrugBatchDTO> getBatchesByDrug(Integer drugId) {
        List<DrugBatch> batches = drugBatchRepository.findByDrugId(drugId);
        return batches.stream().map(this::convertToDrugBatchDTO).collect(Collectors.toList());
    }

    @Override
    public List<DrugBatchDTO> getExpiringBatches(LocalDate daysParam) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);
        List<DrugBatch> batches = drugBatchRepository.findExpiringBatches(today, endDate);
        return batches.stream().map(this::convertToDrugBatchDTO).collect(Collectors.toList());
    }

    @Override
    public List<DrugBatchDTO> getExpiredBatches() {
        List<DrugBatch> batches = drugBatchRepository.findExpiredBatches(LocalDate.now());
        return batches.stream().map(this::convertToDrugBatchDTO).collect(Collectors.toList());
    }

    // ========================== INVENTORY MANAGEMENT ==========================

    @Override
    public Page<InventoryDTO> getInventory(Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findAll(pageable);
        return inventories.map(this::convertToInventoryDTO);
    }

    @Override
    public List<InventoryDTO> getLowStockInventory() {
        Page<Inventory> inventories = inventoryRepository.findLowStockInventory(Pageable.unpaged());
        return inventories.getContent().stream()
            .map(this::convertToInventoryDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDTO> getExpiringInventory() {
        List<Inventory> inventories = inventoryRepository.findExpiringInventory();
        return inventories.stream()
            .map(this::convertToInventoryDTO)
            .collect(Collectors.toList());
    }

    @Override
    public InventoryDTO getInventoryByBatch(Integer batchId) {
        Inventory inventory = inventoryRepository.findByBatchId(batchId)
            .orElseThrow(() -> new RuntimeException("Inventory not found for batchId: " + batchId));
        return convertToInventoryDTO(inventory);
    }

    @Override
    @Transactional
    public void adjustInventory(Integer batchId, Integer quantityChange, String reason, Integer pharmacistId) {
        Inventory inventory = inventoryRepository.findByBatchId(batchId)
            .orElseThrow(() -> new RuntimeException("Inventory not found for batchId: " + batchId));

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        int oldQuantity = inventory.getQuantityInStock();
        int newQuantity = oldQuantity + quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("So luong ton kho khong du sau khi dieu chinh");
        }
        inventory.setQuantityInStock(newQuantity);
        // FIX: Cap nhat trang thai inventory sau khi dieu chinh
        inventory.setStatus(resolveInventoryStatus(newQuantity));
        inventoryRepository.save(inventory);

        InventoryLog logEntry = InventoryLog.builder()
            .batch(inventory.getBatch())
            .user(pharmacist)
            .actionType("ADJUST")
            .quantityChange(quantityChange)
            .quantityBefore(oldQuantity)
            .quantityAfter(newQuantity)
            .referenceId(batchId)
            .referenceType("INVENTORY")
            .notes(reason)
            .build();
        inventoryLogRepository.save(logEntry);

        log.info("Inventory adjusted for batch {}: {} -> {}", batchId, oldQuantity, newQuantity);
    }

    // ========================== DISPENSING ==========================

    @Override
    public Page<PrescriptionDetailDTO> getPendingPrescriptions(Pageable pageable) {
        List<PrescriptionDetail> details = prescriptionDetailRepository.findPendingDispenses();
        List<PrescriptionDetailDTO> dtos = details.stream()
            .map(this::convertToPrescriptionDetailDTO)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    @Override
    public PrescriptionDetailDTO getPrescriptionDetail(Integer detailId) {
        PrescriptionDetail detail = prescriptionDetailRepository.findById(detailId)
            .orElseThrow(() -> new RuntimeException("PrescriptionDetail not found: " + detailId));
        return convertToPrescriptionDetailDTO(detail);
    }

    @Override
    @Transactional
    public void dispenseDrug(DispenseDrugRequest request, Integer pharmacistId) {
        PrescriptionDetail detail = prescriptionDetailRepository.findById(request.getDetailId())
            .orElseThrow(() -> new RuntimeException("PrescriptionDetail not found: " + request.getDetailId()));

        Inventory inventory = inventoryRepository.findByBatchId(request.getBatchId())
            .orElseThrow(() -> new RuntimeException("Inventory not found for batchId: " + request.getBatchId()));

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        if (request.getQuantityDispensed() > inventory.getQuantityInStock()) {
            throw new RuntimeException("So luong ton kho khong du. Ton: "
                + inventory.getQuantityInStock() + ", Can: " + request.getQuantityDispensed());
        }

        // Cap nhat chi tiet don thuoc
        detail.setBatch(inventory.getBatch());
        detail.setQuantityDispensed(request.getQuantityDispensed());
        detail.setActualExpiryDate(inventory.getBatch().getExpiryDate());
        detail.setDispensedAt(LocalDateTime.now());
        detail.setDispensedByUser(pharmacist);
        detail.setNotes(request.getNotes());
        prescriptionDetailRepository.save(detail);

        // Cap nhat ton kho
        int oldQuantity = inventory.getQuantityInStock();
        int newQuantity = oldQuantity - request.getQuantityDispensed();
        inventory.setQuantityInStock(newQuantity);
        // FIX: Cap nhat trang thai inventory sau khi xuat thuoc
        inventory.setStatus(resolveInventoryStatus(newQuantity));
        inventoryRepository.save(inventory);

        // Ghi log xuat kho
        InventoryLog logEntry = InventoryLog.builder()
            .batch(inventory.getBatch())
            .user(pharmacist)
            .actionType("DISPENSE")
            .quantityChange(-request.getQuantityDispensed())
            .quantityBefore(oldQuantity)
            .quantityAfter(newQuantity)
            .referenceId(detail.getDetailId())
            .referenceType("PRESCRIPTION_DETAIL")
            .notes(request.getNotes())
            .build();
        inventoryLogRepository.save(logEntry);

        // FIX: Neu tat ca chi tiet da cap phat het, cap nhat trang thai Prescription -> 1
        Prescription prescription = detail.getPrescription();
        boolean allDispensed = prescription.getDetails().stream()
            .allMatch(d -> d.getQuantityDispensed() != null && d.getQuantityDispensed() > 0);
        if (allDispensed) {
            prescription.setStatus((byte) 1);
            prescriptionRepository.save(prescription);
            log.info("Prescription {} fully dispensed -> status=1", prescription.getPrescriptionCode());
        }

        log.info("Drug dispensed: detail={} | qty={} | batch={}",
            request.getDetailId(), request.getQuantityDispensed(), inventory.getBatch().getBatchNumber());
    }

    @Override
    public Page<PrescriptionDetailDTO> getPrescriptionHistory(Pageable pageable) {
        List<PrescriptionDetail> details = prescriptionDetailRepository.findDispensedDetails();
        List<PrescriptionDetailDTO> dtos = details.stream()
            .map(this::convertToPrescriptionDetailDTO)
            .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    @Override
    public List<DrugBatchDTO> getActiveBatchesForDrug(Integer drugId) {
        List<Inventory> inventories = inventoryRepository.findActiveBatchesByDrugId(drugId);
        return inventories.stream()
            .map(inv -> convertToDrugBatchDTOWithStock(inv.getBatch(), inv))
            .collect(Collectors.toList());
    }

    // ========================== REPORTS ==========================

    @Override
    public List<InventoryLog> getInventoryLog(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return inventoryLogRepository.findByPerformedAtBetween(start, end);
    }

    // ========================== PRIVATE HELPERS ==========================

    /**
     * Xac dinh trang thai inventory dua theo so luong ton kho.
     * 0 = Het hang, 2 = Sap het (< 50 don vi), 1 = Binh thuong
     */
    private Byte resolveInventoryStatus(int quantity) {
        if (quantity <= 0) return (byte) 0;
        if (quantity < 50) return (byte) 2;
        return (byte) 1;
    }

    private DrugDTO convertToDrugDTO(Drug drug) {
        return DrugDTO.builder()
            .drugId(drug.getDrugId())
            .drugCode(drug.getDrugCode())
            .drugName(drug.getDrugName())
            .strength(drug.getStrength())
            .strengthUnit(drug.getStrengthUnit())
            .dosageForm(drug.getDosageForm())
            .routeOfAdministration(drug.getRouteOfAdministration())
            .subCategoryName(drug.getSubCategory().getSubCategoryName())
            .subCategoryId(drug.getSubCategory().getSubCategoryId())
            .packaging(drug.getPackaging())
            .manufacturer(drug.getManufacturer())
            .countryOfOrigin(drug.getCountryOfOrigin())
            .storageCondition(drug.getStorageCondition())
            .shelfLifeMonths(drug.getShelfLifeMonths())
            .notes(drug.getNotes())
            .status(drug.getStatus())
            .build();
    }

    private DrugBatchDTO convertToDrugBatchDTO(DrugBatch batch) {
        Inventory inv = (batch.getInventories() == null || batch.getInventories().isEmpty())
            ? null : batch.getInventories().get(0);
        return convertToDrugBatchDTOWithStock(batch, inv);
    }

    private DrugBatchDTO convertToDrugBatchDTOWithStock(DrugBatch batch, Inventory inv) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
        return DrugBatchDTO.builder()
            .batchId(batch.getBatchId())
            .drugId(batch.getDrug().getDrugId())
            .drugName(batch.getDrug().getDrugName())
            .batchNumber(batch.getBatchNumber())
            .manufactureDate(batch.getManufactureDate())
            .expiryDate(batch.getExpiryDate())
            .unitName(batch.getUnit().getUnitName())
            .quantity(batch.getQuantity())
            .importPrice(batch.getImportPrice())
            .supplier(batch.getSupplier())
            .importDate(batch.getImportDate())
            .importedBy(batch.getImportedByUser().getFullName())
            .status(batch.getStatus())
            .notes(batch.getNotes())
            .quantityInStock(inv != null ? inv.getQuantityInStock() : 0)
            .daysUntilExpiry(daysUntilExpiry)
            .build();
    }

    private InventoryDTO convertToInventoryDTO(Inventory inventory) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), inventory.getBatch().getExpiryDate());
        boolean isExpiringSoon = daysUntilExpiry <= 7 && daysUntilExpiry > 0;
        boolean isLowStock = inventory.getStatus() != null && inventory.getStatus() == 2;

        return InventoryDTO.builder()
            .inventoryId(inventory.getInventoryId())
            .batchId(inventory.getBatch().getBatchId())
            .batchNumber(inventory.getBatch().getBatchNumber())
            .drugId(inventory.getBatch().getDrug().getDrugId())
            .drugName(inventory.getBatch().getDrug().getDrugName())
            .quantityInStock(inventory.getQuantityInStock())
            .lastUpdated(inventory.getLastUpdated())
            .status(inventory.getStatus())
            .daysUntilExpiry(daysUntilExpiry)
            .isExpiringSoon(isExpiringSoon)
            .isLowStock(isLowStock)
            .build();
    }

    private PrescriptionDetailDTO convertToPrescriptionDetailDTO(PrescriptionDetail detail) {
        return PrescriptionDetailDTO.builder()
            .detailId(detail.getDetailId())
            .prescriptionId(detail.getPrescription().getPrescriptionId())
            .prescriptionCode(detail.getPrescription().getPrescriptionCode())
            .drugId(detail.getDrug().getDrugId())
            .drugName(detail.getDrug().getDrugName())
            .dosePerTime(detail.getDosePerTime())
            .timesPerDay(detail.getTimesPerDay())
            .daysOfTreatment(detail.getDaysOfTreatment())
            .quantityPrescribed(detail.getQuantityPrescribed())
            .batchId(detail.getBatch() != null ? detail.getBatch().getBatchId() : null)
            .batchNumber(detail.getBatch() != null ? detail.getBatch().getBatchNumber() : null)
            .batchExpiryDate(detail.getBatch() != null ? detail.getBatch().getExpiryDate() : null)
            .quantityDispensed(detail.getQuantityDispensed())
            .actualExpiryDate(detail.getActualExpiryDate())
            .dispenseUnit(detail.getDispenseUnit())
            .instruction(detail.getInstruction())
            .dispensedAt(detail.getDispensedAt())
            .dispensedByUser(detail.getDispensedByUser() != null ? detail.getDispensedByUser().getFullName() : null)
            .notes(detail.getNotes())
            .patientId(detail.getPrescription().getPatient().getPatientId())
            .patientName(detail.getPrescription().getPatient().getUser().getFullName())
            .isPending(detail.getQuantityDispensed() == null || detail.getQuantityDispensed() == 0)
            .build();
    }
}
