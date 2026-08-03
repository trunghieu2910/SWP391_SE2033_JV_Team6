package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateDrugRequest;
import com.mycompany.jpademo.backend.dto.request.DispenseDrugRequest;
import com.mycompany.jpademo.backend.dto.request.ImportDrugBatchRequest;
import java.math.BigDecimal;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.BatchStatus;
import com.mycompany.jpademo.backend.enums.DrugStatus;
import com.mycompany.jpademo.backend.enums.PrescriptionDetailStatus;
import com.mycompany.jpademo.backend.enums.PrescriptionStatus;
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

    // ========================== BẢNG ĐIỀU KHIỂN ==========================

    @Override
    public PharmacistDashboardDTO getDashboardStats() {
        //Thống kê tổng số lượng (Thuốc & Lô thuốc)
        int totalDrugs = (int) drugRepository.count();
        int totalBatches = (int) drugBatchRepository.count();

        //Đơn thuốc đang chờ xử lý
        List<Prescription> pendingPrescriptions = prescriptionRepository.findByStatus(PrescriptionStatus.PENDING);
        int pendingCount = pendingPrescriptions.size();

        //Thuốc sắp hết hạn (trong 7 ngày tới)
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        List<DrugBatch> expiringBatches = drugBatchRepository.findExpiringBatches(LocalDate.now(), sevenDaysFromNow);
        int expiringCount = expiringBatches.size();

        //Thuốc sắp hết hàng (Low Stock)
        long lowStockCount = inventoryRepository.countLowStock();

        //Đóng gói dữ liệu trả về
        return PharmacistDashboardDTO.builder()
            .totalDrugs(totalDrugs)
            .pendingPrescriptions(pendingCount)
            .expiringDrugs(expiringCount)
            .lowStockDrugs((int) lowStockCount)
            .totalBatches(totalBatches)
            .lastUpdate(LocalDateTime.now())
            .build();
    }

    // ========================== QUẢN LÝ THUỐC ==========================

    //Lấy danh sách thuốc có phân trang
    @Override
    public Page<DrugDTO> getDrugList(Pageable pageable) {
        Page<Drug> drugs = drugRepository.findAll(pageable);
        return drugs.map(this::convertToDrugDTO);
    }

    //Tìm kiếm thuốc theo từ khóa
    @Override
    public Page<DrugDTO> searchDrugs(String search, Pageable pageable) {
        Page<Drug> drugs = drugRepository.searchDrugs(search, pageable);
        return drugs.map(this::convertToDrugDTO);
    }

    //Lấy tất cả thuốc đang hoạt động
    @Override
    public List<DrugDTO> getAllActiveDrugs() {
        List<Drug> drugs = drugRepository.findByStatus(DrugStatus.ACTIVE);
        return drugs.stream().map(this::convertToDrugDTO).collect(java.util.stream.Collectors.toList());
    }

    //Lọc thuốc theo danh mục con
    @Override
    public Page<DrugDTO> getDrugsByCategory(Integer subCategoryId, Pageable pageable) {
        Page<Drug> drugs = drugRepository.findBySubCategoryId(subCategoryId, pageable);
        return drugs.map(this::convertToDrugDTO);
    }

    //Lấy danh sách đơn vị quy đổi của thuốc
    @Override
    public List<DrugConversionDTO> getDrugConversions(Integer drugId) {
        if (drugId == null) {
            return List.of();
        }
        return unitConversionRepository.findByDrugId(drugId).stream()
            .map(this::convertToDrugConversionDTO)
            .collect(Collectors.toList());
    }

    //Xem chi tiết một loại thuốc
    @Override
    public DrugDTO getDrugDetail(Integer drugId) {
        Drug drug = drugRepository.findById(drugId)
            .orElseThrow(() -> new RuntimeException("Drug not found: " + drugId));
        return convertToDrugDTO(drug);
    }

    //Tạo thuốc mới
    @Override
    @Transactional
    public DrugDTO createDrug(CreateDrugRequest request, Integer pharmacistId) {
        if (request.getBaseUnitId() == null) {
            throw new RuntimeException("Vui lòng chọn Đơn vị gốc kê đơn.");
        }

        // Validate mã thuốc do người dùng nhập
        String drugCode = request.getDrugCode();
        if (drugCode == null || drugCode.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập mã thuốc.");
        }
        drugCode = drugCode.trim().toUpperCase();
        if (drugCode.length() > 8) {
            throw new RuntimeException("Mã thuốc không được vượt quá 8 ký tự.");
        }
        if (!drugCode.matches("^[A-Z0-9\\-]+$")) {
            throw new RuntimeException("Mã thuốc chỉ được chứa chữ cái, số và dấu gạch ngang (-).");
        }
        if (drugRepository.findByDrugCode(drugCode).isPresent()) {
            throw new RuntimeException("Mã thuốc \"" + drugCode + "\" đã tồn tại. Vui lòng chọn mã khác.");
        }
        request.setDrugCode(drugCode);

        validateDrugRequest(request, request.getBaseUnitId());

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        DrugSubCategory subCategory = drugSubCategoryRepository.findById(request.getSubCategoryId())
            .orElseThrow(() -> new RuntimeException("SubCategory not found: " + request.getSubCategoryId()));

        Unit baseUnit = unitRepository.findById(request.getBaseUnitId())
            .orElseThrow(() -> new RuntimeException("Base Unit not found: " + request.getBaseUnitId()));

        Drug drug = Drug.builder()
            .drugCode(request.getDrugCode())
            .drugName(request.getDrugName())
            .strength(request.getStrength())
            .strengthUnit(request.getStrengthUnit())
            .dosageForm(request.getDosageForm())
            .routeOfAdministration(request.getRouteOfAdministration())
            .subCategory(subCategory)
            .baseUnit(baseUnit)
            .manufacturer(request.getManufacturer())
            .countryOfOrigin(request.getCountryOfOrigin())
            .storageCondition(request.getStorageCondition())
            .notes(request.getNotes())
            .status(DrugStatus.ACTIVE)
            .createdByUser(pharmacist)
            .build();

        Drug savedDrug = drugRepository.save(drug);

        // Lưu thiết lập quy đổi đơn vị
        saveUnitConversions(savedDrug, request);

        log.info("Drug created: {}", savedDrug.getDrugCode());
        return convertToDrugDTO(savedDrug);
    }

    @Override
    @Transactional
    public DrugDTO updateDrug(Integer drugId, CreateDrugRequest request, Integer pharmacistId) {
        Drug drug = drugRepository.findById(drugId)
            .orElseThrow(() -> new RuntimeException("Drug not found: " + drugId));

        Integer baseUnitIdToValidate = request.getBaseUnitId() != null ? request.getBaseUnitId() : drug.getBaseUnit().getUnitId();
        validateDrugRequest(request, baseUnitIdToValidate);

        // Chặn chỉnh sửa nếu thuốc đã Ngừng dùng (status == INACTIVE)
        if (drug.getStatus() == DrugStatus.INACTIVE) {
            throw new RuntimeException("Không thể sửa thông tin thuốc đã Ngừng dùng.");
        }

        DrugSubCategory subCategory = drugSubCategoryRepository.findById(request.getSubCategoryId())
            .orElseThrow(() -> new RuntimeException("SubCategory not found: " + request.getSubCategoryId()));

        boolean hasBatches = drug.getBatches() != null && !drug.getBatches().isEmpty();
        
        if (hasBatches) {
            if (request.getBaseUnitId() != null && !drug.getBaseUnit().getUnitId().equals(request.getBaseUnitId())) {
                throw new RuntimeException("Không thể sửa Đơn vị gốc kê đơn vì thuốc đã có lô hàng nhập kho.");
            }
        } else {
            Unit baseUnit = unitRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new RuntimeException("Base Unit not found: " + request.getBaseUnitId()));
            drug.setBaseUnit(baseUnit);
        }

        drug.setDrugName(request.getDrugName());
        drug.setStrength(request.getStrength());
        drug.setStrengthUnit(request.getStrengthUnit());
        drug.setDosageForm(request.getDosageForm());
        drug.setRouteOfAdministration(request.getRouteOfAdministration());
        drug.setSubCategory(subCategory);
        drug.setManufacturer(request.getManufacturer());
        drug.setCountryOfOrigin(request.getCountryOfOrigin());
        drug.setStorageCondition(request.getStorageCondition());
        drug.setNotes(request.getNotes());

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));
        drug.setUpdatedByUser(pharmacist);

        Drug updatedDrug = drugRepository.save(drug);

        if (!hasBatches) {
            updatedDrug.getConversions().clear();
            drugRepository.flush();
            saveUnitConversions(updatedDrug, request);
        }

        log.info("Drug updated: {}", updatedDrug.getDrugCode());
        return convertToDrugDTO(updatedDrug);
    }

    private void saveUnitConversions(Drug drug, CreateDrugRequest request) {
        if (request.getConversionLargeUnitIds() != null && request.getConversionSmallUnitIds() != null && request.getConversionQuantities() != null) {
            int size = Math.min(request.getConversionLargeUnitIds().size(), 
                       Math.min(request.getConversionSmallUnitIds().size(), request.getConversionQuantities().size()));
            
            for (int i = 0; i < size; i++) {
                Integer largeUnitId = request.getConversionLargeUnitIds().get(i);
                Integer smallUnitId = request.getConversionSmallUnitIds().get(i);
                Integer qty = request.getConversionQuantities().get(i);
                
                if (largeUnitId != null && smallUnitId != null && qty != null && qty > 0) {
                    Unit largeUnit = unitRepository.findById(largeUnitId).orElse(null);
                    Unit smallUnit = unitRepository.findById(smallUnitId).orElse(null);
                    if (largeUnit != null && smallUnit != null) {
                        UnitConversion conversion = new UnitConversion();
                        conversion.setDrug(drug);
                        conversion.setLargeUnit(largeUnit);
                        conversion.setSmallUnit(smallUnit);
                        conversion.setConversionQuantity(qty);
                        unitConversionRepository.save(conversion);
                    }
                }
            }
        }
    }

    private void validateDrugRequest(CreateDrugRequest request, Integer baseUnitId) {
        if (request.getDrugName() == null || request.getDrugName().trim().isEmpty() ||
            request.getStrength() == null ||
            request.getStrengthUnit() == null || request.getStrengthUnit().trim().isEmpty() ||
            request.getDosageForm() == null || request.getDosageForm().trim().isEmpty() ||
            request.getRouteOfAdministration() == null || request.getRouteOfAdministration().trim().isEmpty() ||
            request.getSubCategoryId() == null ||
            request.getManufacturer() == null || request.getManufacturer().trim().isEmpty() ||
            request.getCountryOfOrigin() == null || request.getCountryOfOrigin().trim().isEmpty() ||
            request.getStorageCondition() == null || request.getStorageCondition().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng điền đầy đủ các thông tin bắt buộc.");
        }
        
        try {
            double strengthValue = Double.parseDouble(request.getStrength().trim());
            if (strengthValue < 0) {
                throw new RuntimeException("Hàm lượng thuốc không được nhập số âm.");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Hàm lượng thuốc phải là một số hợp lệ.");
        }

        if (baseUnitId == null) {
            throw new RuntimeException("Vui lòng chọn Đơn vị gốc kê đơn.");
        }

        boolean hasValidConversion = false;
        java.util.Map<Integer, Integer> conversionMap = new java.util.HashMap<>();
        java.util.Set<Integer> allSmallUnits = new java.util.HashSet<>();
        java.util.Set<Integer> allUsedUnits = new java.util.HashSet<>();

        if (request.getConversionLargeUnitIds() != null && request.getConversionSmallUnitIds() != null && request.getConversionQuantities() != null) {
            int size = Math.min(request.getConversionLargeUnitIds().size(), 
                       Math.min(request.getConversionSmallUnitIds().size(), request.getConversionQuantities().size()));
            for (int i = 0; i < size; i++) {
                Integer largeUnitId = request.getConversionLargeUnitIds().get(i);
                Integer smallUnitId = request.getConversionSmallUnitIds().get(i);
                Integer qty = request.getConversionQuantities().get(i);
                
                if (largeUnitId != null && smallUnitId != null && qty != null && qty > 0) {
                    hasValidConversion = true;
                    if (conversionMap.containsKey(largeUnitId)) {
                        throw new RuntimeException("Đơn vị quy đổi lớn (cái trên) không được trùng lặp.");
                    }
                    if (!allSmallUnits.add(smallUnitId)) {
                        throw new RuntimeException("Đơn vị quy đổi nhỏ (cái dưới) không được trùng lặp. Các đơn vị phải tạo thành một chuỗi duy nhất.");
                    }
                    if (largeUnitId.equals(smallUnitId)) {
                        throw new RuntimeException("Đơn vị quy đổi lớn và nhỏ không được giống nhau.");
                    }
                    conversionMap.put(largeUnitId, smallUnitId);
                    allUsedUnits.add(largeUnitId);
                    allUsedUnits.add(smallUnitId);
                }
            }
        }
        
        if (!hasValidConversion) {
            throw new RuntimeException("Vui lòng thiết lập ít nhất một bước quy đổi đơn vị.");
        }

        if (conversionMap.containsKey(baseUnitId)) {
            throw new RuntimeException("Đơn vị gốc (Base Unit) phải là đơn vị nhỏ nhất, không thể là đơn vị lớn (cái trên) để quy đổi tiếp.");
        }

        for (Integer unitId : allUsedUnits) {
            if (unitId.equals(baseUnitId)) {
                continue;
            }
            Integer current = unitId;
            java.util.Set<Integer> visited = new java.util.HashSet<>();
            boolean reachedBase = false;
            while (current != null) {
                if (current.equals(baseUnitId)) {
                    reachedBase = true;
                    break;
                }
                if (!visited.add(current)) {
                    throw new RuntimeException("Phát hiện vòng lặp trong quy đổi đơn vị.");
                }
                current = conversionMap.get(current);
            }
            if (!reachedBase) {
                throw new RuntimeException("Lỗi cấu hình: Các đơn vị quy đổi phải nối tiếp nhau và liên kết trực tiếp tới Đơn vị gốc (VD: Hộp -> Vỉ -> Viên). Không được để đứt quãng (VD: Hộp -> Vỉ, Lọ -> Viên).");
            }
        }
    }

    @Override
    @Transactional
    public void updateDrugStatus(Integer drugId, DrugStatus status) {
        Drug drug = drugRepository.findById(drugId)
            .orElseThrow(() -> new RuntimeException("Drug not found: " + drugId));
        drug.setStatus(status);
        drugRepository.save(drug);

        // Cập nhật trạng thái cho các lô hàng liên quan
        if (status == DrugStatus.INACTIVE) {
            // Khóa tất cả các lô hàng của thuốc này
            List<DrugBatch> batches = drugBatchRepository.findByDrugId(drugId);
            for (DrugBatch batch : batches) {
                batch.setStatus(BatchStatus.INACTIVE);
                drugBatchRepository.save(batch);
            }
        } else if (status == DrugStatus.ACTIVE) {
            // Kích hoạt lại các lô hàng chưa hết hạn của thuốc này
            LocalDate today = LocalDate.now();
            List<DrugBatch> batches = drugBatchRepository.findByDrugId(drugId);
            for (DrugBatch batch : batches) {
                if (batch.getExpiryDate().isAfter(today)) {
                    batch.setStatus(BatchStatus.ACTIVE);
                    drugBatchRepository.save(batch);
                }
            }
        }
        log.info("Drug status updated: {} to {}", drug.getDrugCode(), status);
    }

    @Override
    public String generateNextDrugCode() {
        List<String> codes = drugRepository.findAllDrugCodes();
        java.util.Set<Integer> existingIds = new java.util.HashSet<>();
        for (String code : codes) {
            if (code != null && code.toUpperCase().startsWith("DRUG-")) {
                try {
                    String numStr = code.substring(5).trim();
                    int id = Integer.parseInt(numStr);
                    existingIds.add(id);
                } catch (NumberFormatException e) {
                    // Bỏ qua các mã thuốc không phải là số
                }
            }
        }
        int nextId = 1;
        while (existingIds.contains(nextId)) {
            nextId++;
        }
        return String.format("DRUG-%03d", nextId);
    }

    @Override
    public String generateNextBatchStt() {
        List<String> batchNumbers = drugBatchRepository.findAllBatchNumbers();
        java.util.Set<Integer> existingStts = new java.util.HashSet<>();
        for (String batchNum : batchNumbers) {
            if (batchNum != null && batchNum.toUpperCase().startsWith("LOT-")) {
                String[] parts = batchNum.split("-");
                if (parts.length >= 2) {
                    try {
                        int stt = Integer.parseInt(parts[1]);
                        existingStts.add(stt);
                    } catch (NumberFormatException e) {
                        // Bỏ qua các phần tử STT không phải là số
                    }
                }
            }
        }
        int nextStt = 1;
        while (existingStts.contains(nextStt)) {
            nextStt++;
        }
        return String.format("%03d", nextStt);
    }

    // ========================== QUẢN LÝ LÔ HÀNG ==========================

    @Override
    public Page<DrugBatchDTO> getDrugBatches(Pageable pageable) {
        Page<DrugBatch> batches = drugBatchRepository.findAll(pageable);
        return batches.map(this::convertToDrugBatchDTO);
    }

    @Override
    @Transactional
    public DrugBatchDTO createDrugBatch(ImportDrugBatchRequest request, Integer pharmacistId) {
        // Kiểm tra các trường bắt buộc (ngoại trừ ghi chú)
        if (request.getDrugId() == null || request.getBatchNumber() == null || request.getBatchNumber().trim().isEmpty() ||
            request.getManufactureDate() == null || request.getExpiryDate() == null || request.getUnitId() == null ||
            request.getQuantity() == null || request.getSupplier() == null || request.getSupplier().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng điền đầy đủ tất cả các trường bắt buộc (ngoại trừ ghi chú).");
        }

        // Kiểm tra tính hợp lệ của ngày sản xuất
        LocalDate today = LocalDate.now();
        if (!request.getManufactureDate().isBefore(today)) {
            throw new RuntimeException("Ngày sản xuất phải nhỏ hơn ngày hiện tại (ngày trong quá khứ).");
        }

        // Kiểm tra tính hợp lệ của hạn sử dụng
        if (!request.getExpiryDate().isAfter(today)) {
            throw new RuntimeException("Hạn sử dụng phải lớn hơn ngày hiện tại (ngày trong tương lai).");
        }
        if (!request.getExpiryDate().isAfter(request.getManufactureDate())) {
            throw new RuntimeException("Hạn sử dụng phải lớn hơn ngày sản xuất.");
        }

        // Kiểm tra số lượng nhập
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng nhập phải là số nguyên dương lớn hơn 0.");
        }


        Drug drug = drugRepository.findById(request.getDrugId())
            .orElseThrow(() -> new RuntimeException("Drug not found: " + request.getDrugId()));

        // Chặn nhập lô hàng nếu thuốc đã Ngừng dùng (status == INACTIVE)
        if (drug.getStatus() == DrugStatus.INACTIVE) {
            throw new RuntimeException("Không thể nhập kho thuốc '"
                + drug.getDrugName() + "' vì thuốc này đã Ngừng dùng.");
        }

        Unit unit = unitRepository.findById(request.getUnitId())
            .orElseThrow(() -> new RuntimeException("Unit not found: " + request.getUnitId()));

        // Validate unit must be in drug's unit conversions or base unit
        List<UnitConversion> conversions = unitConversionRepository.findByDrugId(drug.getDrugId());
        boolean isUnitValid = false;
        if (drug.getBaseUnit().getUnitId().equals(unit.getUnitId())) {
            isUnitValid = true;
        } else {
            for (UnitConversion conv : conversions) {
                if ((conv.getLargeUnit() != null && conv.getLargeUnit().getUnitId().equals(unit.getUnitId())) ||
                        (conv.getSmallUnit() != null && conv.getSmallUnit().getUnitId().equals(unit.getUnitId()))) {
                    isUnitValid = true;
                    break;
                }
            }
        }
        if (!isUnitValid) {
            throw new RuntimeException("Đơn vị nhập không hợp lệ. Phải chọn đúng những đơn vị đã được ghi trong thiết lập quy đổi của thuốc.");
        }

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        DrugBatch batch = DrugBatch.builder()
            .drug(drug)
            .batchNumber(request.getBatchNumber())
            .manufactureDate(request.getManufactureDate())
            .expiryDate(request.getExpiryDate())
            .unit(unit)
            .quantity(request.getQuantity())
            .supplier(request.getSupplier())
            .importedByUser(pharmacist)
            .status(BatchStatus.ACTIVE)
            .notes(request.getNotes())
            .build();

        DrugBatch savedBatch = drugBatchRepository.save(batch);

        int quantityInSmallUnit = calculateSmallUnitQuantityFromRequest(drug, unit, request.getQuantity(), request.getPackagingChain());

        Inventory inventory = Inventory.builder()
            .batch(savedBatch)
            .quantityInStock(quantityInSmallUnit)
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

    @Override
    public DrugBatchDTO getDrugBatchDetail(Integer batchId) {
        DrugBatch batch = drugBatchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));
        return convertToDrugBatchDTO(batch);
    }

    @Override
    @Transactional
    public DrugBatchDTO updateDrugBatch(Integer batchId, ImportDrugBatchRequest request, Integer pharmacistId) {
        // Kiểm tra các trường bắt buộc
        if (request.getQuantity() == null || request.getSupplier() == null || request.getSupplier().trim().isEmpty() ||
            request.getManufactureDate() == null || request.getExpiryDate() == null || request.getUnitId() == null) {
            throw new RuntimeException("Vui lòng điền đầy đủ tất cả các trường bắt buộc.");
        }

        // Kiểm tra ghi chú (Bắt buộc phải có lý do cập nhật)
        if (request.getNotes() == null || request.getNotes().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập ghi chú (lý do sửa đổi) khi chỉnh sửa thông tin lô hàng.");
        }

        // Kiểm tra tính hợp lệ của ngày tháng
        if (request.getManufactureDate().isAfter(LocalDate.now()) || request.getManufactureDate().isEqual(LocalDate.now())) {
            throw new RuntimeException("Ngày sản xuất phải là ngày trong quá khứ.");
        }
        if (request.getExpiryDate().isBefore(LocalDate.now()) || request.getExpiryDate().isEqual(LocalDate.now())) {
            throw new RuntimeException("Hạn sử dụng phải là ngày trong tương lai.");
        }
        if (request.getExpiryDate().isBefore(request.getManufactureDate()) || request.getExpiryDate().isEqual(request.getManufactureDate())) {
            throw new RuntimeException("Hạn sử dụng phải lớn hơn Ngày sản xuất.");
        }

        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng nhập phải là số nguyên dương lớn hơn 0.");
        }


        DrugBatch batch = drugBatchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Lô hàng không tồn tại: " + batchId));

        // Chặn chỉnh sửa nếu thuốc đã Ngừng dùng
        Drug drug = batch.getDrug();
        if (drug.getStatus() == DrugStatus.INACTIVE) {
            throw new RuntimeException("không được chỉnh sửa: lô hàng có thuốc đã ngưng sử dụng");
        }

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        Inventory inventory = inventoryRepository.findByBatch_BatchId(batchId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho lô hàng này."));

        // Tìm kiếm và gán đơn vị mới
        Unit unit = unitRepository.findById(request.getUnitId())
            .orElseThrow(() -> new RuntimeException("Đơn vị tính không hợp lệ: " + request.getUnitId()));

        // Validate unit must be in drug's unit conversions or base unit
        List<UnitConversion> conversions = unitConversionRepository.findByDrugId(drug.getDrugId());
        boolean isUnitValid = false;
        if (drug.getBaseUnit().getUnitId().equals(unit.getUnitId())) {
            isUnitValid = true;
        } else {
            for (UnitConversion conv : conversions) {
                if ((conv.getLargeUnit() != null && conv.getLargeUnit().getUnitId().equals(unit.getUnitId())) ||
                        (conv.getSmallUnit() != null && conv.getSmallUnit().getUnitId().equals(unit.getUnitId()))) {
                    isUnitValid = true;
                    break;
                }
            }
        }
        if (!isUnitValid) {
            throw new RuntimeException("Đơn vị nhập không hợp lệ. Phải chọn đúng những đơn vị đã được ghi trong thiết lập quy đổi của thuốc.");
        }

        int newSmallQty = calculateSmallUnitQuantityFromRequest(drug, unit, request.getQuantity(), request.getPackagingChain());
        int oldSmallQty = calculateSmallUnitQuantity(drug.getDrugId(), batch.getUnit().getUnitId(), batch.getQuantity());
        
        int currentStock = inventory.getQuantityInStock();
        int usedSmallQty = oldSmallQty - currentStock;

        if (newSmallQty < usedSmallQty) {
            int factor = newSmallQty / (request.getQuantity() > 0 ? request.getQuantity() : 1);
            int usedLargeQty = factor > 0 ? usedSmallQty / factor : 0;
            throw new RuntimeException("Không thể lưu: Số lượng mới nhỏ hơn số lượng đã sử dụng (đã dùng: khoảng " 
                + usedLargeQty + " đơn vị lớn, tương đương " + usedSmallQty + " đơn vị nhỏ)!");
        }

        int newStock = currentStock + (newSmallQty - oldSmallQty);

        // Cập nhật thông tin lô hàng
        batch.setManufactureDate(request.getManufactureDate());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setUnit(unit);
        batch.setQuantity(request.getQuantity());
        batch.setSupplier(request.getSupplier());
        batch.setNotes(request.getNotes());
        batch.setUpdateReason(request.getUpdateReason());
        batch.setUpdatedByUser(pharmacist);
        batch.setUpdatedAt(LocalDateTime.now());
        drugBatchRepository.save(batch);

        // Cập nhật thông tin tồn kho
        inventory.setQuantityInStock(newStock);
        inventoryRepository.save(inventory);

        // Ghi log thay đổi
        InventoryLog logEntry = InventoryLog.builder()
            .batch(batch)
            .user(pharmacist)
            .actionType("ADJUST")
            .quantityChange(newSmallQty - oldSmallQty)
            .quantityBefore(currentStock)
            .quantityAfter(newStock)
            .referenceId(batch.getBatchId())
            .referenceType("BATCH_UPDATE")
            .notes("Cập nhật lô hàng: " + request.getNotes())
            .build();
        inventoryLogRepository.save(logEntry);

        log.info("Drug batch updated: {} - {} | new qty: {}, new stock: {}", 
            drug.getDrugCode(), batch.getBatchNumber(), request.getQuantity(), newStock);

        return convertToDrugBatchDTO(batch);
    }

    private int getConversionFactor(Integer drugId, Integer largeUnitId) {
        List<UnitConversion> conversions = unitConversionRepository.findByDrugId(drugId);
        return conversions.stream()
            .filter(c -> c.getLargeUnit().getUnitId().equals(largeUnitId))
            .map(UnitConversion::getConversionQuantity)
            .findFirst()
            .orElse(1);
    }

    /**
     * Tinh so luong quy doi sang don vi nho nhat dua vao bang UnitConversion.
     * Neu khong tim thay quy doi, dung nguyen so luong nhap.
     */
    private int calculateSmallUnitQuantity(Integer drugId, Integer largeUnitId, int quantity) {
        List<UnitConversion> conversions = unitConversionRepository.findByDrugId(drugId);
        int convertedQuantity = calculateSmallUnitQuantityFromConversions(conversions, largeUnitId, quantity);
        if (convertedQuantity != quantity) {
            log.debug("Quy doi: {} -> {} don vi nho", quantity, convertedQuantity);
        } else {
            log.warn("Khong tim thay quy doi don vi cho drugId={}, unitId={}. Dung nguyen so luong.", drugId, largeUnitId);
        }
        return convertedQuantity;
    }

    public static int calculateSmallUnitQuantityFromConversions(List<UnitConversion> conversions, Integer largeUnitId, int quantity) {
        if (conversions == null || conversions.isEmpty() || largeUnitId == null || quantity <= 0) {
            return quantity;
        }

        java.util.Map<Integer, List<UnitConversion>> conversionsByLargeUnit = conversions.stream()
            .filter(c -> c.getLargeUnit() != null && c.getSmallUnit() != null)
            .collect(Collectors.groupingBy(c -> c.getLargeUnit().getUnitId()));

        return calculateSmallUnitQuantityByUnit(conversionsByLargeUnit, largeUnitId, quantity);
    }

    public static int calculateSmallUnitQuantityFromChain(String packagingChain, int quantity) {
        if (quantity <= 0 || packagingChain == null || packagingChain.trim().isEmpty()) {
            return quantity;
        }

        String normalized = packagingChain.replace(" ", "").trim();
        String[] parts = normalized.split(",");
        int result = quantity;

        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) {
                continue;
            }
            try {
                result *= Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                return quantity;
            }
        }

        return result;
    }

    private int calculateSmallUnitQuantityFromRequest(Drug drug, Unit unit, Integer quantity, String packagingChain) {
        if (quantity == null || quantity <= 0) {
            return 0;
        }

        if (packagingChain != null && !packagingChain.trim().isEmpty()) {
            return calculateSmallUnitQuantityFromChain(packagingChain, quantity);
        }

        return calculateSmallUnitQuantity(drug.getDrugId(), unit.getUnitId(), quantity);
    }

    private static int calculateSmallUnitQuantityByUnit(java.util.Map<Integer, List<UnitConversion>> conversionsByLargeUnit,
                                                        Integer currentUnitId, int quantity) {
        if (currentUnitId == null) {
            return quantity;
        }

        List<UnitConversion> nextSteps = conversionsByLargeUnit.get(currentUnitId);
        if (nextSteps == null || nextSteps.isEmpty()) {
            return quantity;
        }

        UnitConversion nextStep = nextSteps.stream().findFirst().orElse(null);
        if (nextStep == null || nextStep.getConversionQuantity() == null) {
            return quantity;
        }

        int nextQuantity = quantity * nextStep.getConversionQuantity();
        return calculateSmallUnitQuantityByUnit(conversionsByLargeUnit, nextStep.getSmallUnit() != null ? nextStep.getSmallUnit().getUnitId() : null, nextQuantity);
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

    // ========================== QUẢN LÝ TỒN KHO ==========================

    @Override
    @Transactional
    public Page<InventoryDTO> getInventory(String keyword, String unit, Pageable pageable) {
        // Tự động cập nhật lô hàng hết hạn (status = EXPIRED) khi xem tồn kho
        LocalDate today = LocalDate.now();
        List<DrugBatch> expiredBatches = drugBatchRepository.findExpiredBatches(today);
        for (DrugBatch batch : expiredBatches) {
            batch.setStatus(BatchStatus.EXPIRED);
            drugBatchRepository.save(batch);
        }

        Page<Inventory> inventories = inventoryRepository.searchInventory(
            keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : "",
            unit != null && !unit.trim().isEmpty() ? unit.trim() : "",
            pageable
        );
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
        java.time.LocalDate threshold = java.time.LocalDate.now().plusDays(7);
        List<Inventory> inventories = inventoryRepository.findExpiringInventory(threshold);
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

    @Override
    public List<String> getDistinctSmallUnits() {
        return inventoryRepository.findDistinctSmallUnits();
    }

    // ========================== CẤP PHÁT THUỐC ==========================

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
    public org.springframework.data.domain.Page<PrescriptionSummaryDTO> getPendingPrescriptionsSummary(
            String keyword, String status, LocalDate fromDate, LocalDate toDate, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        
        String safeKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String safeStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        LocalDateTime startDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (toDate != null) ? toDate.atTime(23, 59, 59) : null;
        
        org.springframework.data.domain.Page<Prescription> prescriptionPage = prescriptionRepository.filterDispensePrescriptionsPage(
                safeKeyword, safeStatus, startDateTime, endDateTime, pageable);

        return prescriptionPage.map(p -> {
            List<PrescriptionDetail> items = prescriptionDetailRepository.findByPrescription_PrescriptionId(p.getPrescriptionId());
            if (items == null || items.isEmpty()) {
                return PrescriptionSummaryDTO.builder()
                    .prescriptionId(p.getPrescriptionId())
                    .prescriptionCode(p.getPrescriptionCode())
                    .patientName(p.getPatient().getUser().getFullName())
                    .prescriptionDate(p.getPrescriptionDate())
                    .diagnosis(p.getDiagnosis())
                    .status(p.getStatus())
                    .totalItems(0)
                    .pendingItems(0)
                    .isPending(false)
                    .build();
            }

            List<PrescriptionDetailDTO> dtos = items.stream()
                .map(this::convertToPrescriptionDetailDTO)
                .collect(Collectors.toList());
            long pending = dtos.stream().filter(d -> Boolean.TRUE.equals(d.getIsPending())).count();

            return PrescriptionSummaryDTO.builder()
                .prescriptionId(p.getPrescriptionId())
                .prescriptionCode(p.getPrescriptionCode())
                .patientName(p.getPatient().getUser().getFullName())
                .prescriptionDate(p.getPrescriptionDate())
                .diagnosis(p.getDiagnosis())
                .status(pending > 0 ? p.getStatus() : com.mycompany.jpademo.backend.enums.PrescriptionStatus.DISPENSED)
                .totalItems(items.size())
                .pendingItems((int) pending)
                .isPending(pending > 0)
                .details(dtos)
                .build();
        });
    }

    //@Override
    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDetailDTO> getPrescriptionDetailsByPrescriptionId(Integer prescriptionId) {
        List<PrescriptionDetail> details = prescriptionDetailRepository.findByPrescription_PrescriptionId(prescriptionId);
        return details.stream().map(this::convertToPrescriptionDetailDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDetailDTO getPrescriptionDetail(Integer detailId) {
        log.debug("Getting prescription detail for detailId={}", detailId);
        PrescriptionDetail detail = prescriptionDetailRepository.findById(detailId)
            .orElseThrow(() -> new RuntimeException("PrescriptionDetail not found: " + detailId));
        PrescriptionDetailDTO dto = convertToPrescriptionDetailDTO(detail);
        log.debug("PrescriptionDetailDTO: drugId={}, detailId={}", dto.getDrugId(), dto.getDetailId());
        return dto;
    }

    @Override
    @Transactional
    public void dispenseDrug(DispenseDrugRequest request, Integer pharmacistId) {
        PrescriptionDetail detail = prescriptionDetailRepository.findById(request.getDetailId())
            .orElseThrow(() -> new RuntimeException("PrescriptionDetail not found: " + request.getDetailId()));

        User pharmacist = userRepository.findById(pharmacistId)
            .orElseThrow(() -> new RuntimeException("User not found: " + pharmacistId));

        if (request.getActionStatus() == PrescriptionDetailStatus.CANCELLED) {
            if (request.getNotes() == null || request.getNotes().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập ghi chú (lý do hủy) khi thay đổi trạng thái thành Hủy.");
            }
            detail.setStatus(PrescriptionDetailStatus.CANCELLED);
            detail.setNotes(request.getNotes().trim());
            detail.setDispensedAt(LocalDateTime.now());
            detail.setDispensedByUser(pharmacist);
            prescriptionDetailRepository.save(detail);

            Prescription prescription = detail.getPrescription();
            boolean allFinished = prescription.getDetails().stream()
                .allMatch(d -> d.getStatus() == PrescriptionDetailStatus.DISPENSED || d.getStatus() == PrescriptionDetailStatus.CANCELLED || (d.getQuantityDispensed() != null && d.getQuantityDispensed() > 0));
            if (allFinished) {
                prescription.setStatus(PrescriptionStatus.DISPENSED);
                prescriptionRepository.save(prescription);
            }
            log.info("Prescription detail {} cancelled by pharmacist {}", request.getDetailId(), pharmacistId);
            return;
        }

        if (request.getBatchId() == null) {
            throw new RuntimeException("Vui lòng chọn lô hàng khi cấp phát thuốc.");
        }

        Inventory inventory = inventoryRepository.findByBatchId(request.getBatchId())
            .orElseThrow(() -> new RuntimeException("Inventory not found for batchId: " + request.getBatchId()));

        DrugStatus drugStatus = inventory.getBatch().getDrug().getStatus();
        BatchStatus batchStatus = inventory.getBatch().getStatus();
        if (drugStatus == DrugStatus.INACTIVE || batchStatus == BatchStatus.INACTIVE || batchStatus == BatchStatus.EXPIRED) {
            throw new RuntimeException("Thuốc hoặc lô hàng này đang ở trạng thái ngưng sử dụng hoặc hết hạn, không được phép phát thuốc.");
        }

        if (request.getQuantityDispensed() > inventory.getQuantityInStock()) {
            throw new RuntimeException("So luong ton kho khong du. Ton: "
                + inventory.getQuantityInStock() + ", Can: " + request.getQuantityDispensed());
        }

        // Quy đổi số lượng cấp phát từ đơn vị kê đơn sang đơn vị gốc
        int factor = 1;
        String dispenseUnitName = detail.getDispenseUnit();
        if (dispenseUnitName != null && !dispenseUnitName.trim().isEmpty()) {
            Unit largeUnit = unitRepository.findAll().stream()
                .filter(u -> getSanitizedUnitName(u).equalsIgnoreCase(dispenseUnitName.trim()))
                .findFirst().orElse(null);
            if (largeUnit != null) {
                factor = calculateSmallUnitQuantity(detail.getDrug().getDrugId(), largeUnit.getUnitId(), 1);
            }
        }
        int dispensedQuantityInSmallUnit = request.getQuantityDispensed() * factor;

        if (dispensedQuantityInSmallUnit > inventory.getQuantityInStock()) {
            throw new RuntimeException("So luong ton kho khong du. Ton: "
                + inventory.getQuantityInStock() + ", Can: " + dispensedQuantityInSmallUnit);
        }

        // Cập nhật chi tiết đơn thuốc
        detail.setBatch(inventory.getBatch());
        detail.setQuantityDispensed(request.getQuantityDispensed());
        detail.setActualExpiryDate(inventory.getBatch().getExpiryDate());
        detail.setDispensedAt(LocalDateTime.now());
        detail.setDispensedByUser(pharmacist);
        detail.setNotes(request.getNotes());
        detail.setStatus(PrescriptionDetailStatus.DISPENSED);
        prescriptionDetailRepository.save(detail);

        // Cập nhật tồn kho
        int oldQuantity = inventory.getQuantityInStock();
        int newQuantity = oldQuantity - dispensedQuantityInSmallUnit;
        inventory.setQuantityInStock(newQuantity);
        inventoryRepository.save(inventory);

        // Ghi log xuất kho
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

        // Nếu tất cả chi tiết đã cấp phát hết, cập nhật trạng thái Đơn thuốc -> ĐÃ CẤP PHÁT (DISPENSED)
        Prescription prescription = detail.getPrescription();
        boolean allDispensed = prescription.getDetails().stream()
            .allMatch(d -> d.getStatus() == PrescriptionDetailStatus.DISPENSED || (d.getQuantityDispensed() != null && d.getQuantityDispensed() > 0));
        if (allDispensed) {
            prescription.setStatus(PrescriptionStatus.DISPENSED);
            prescriptionRepository.save(prescription);
            log.info("Prescription {} fully dispensed -> status=DISPENSED", prescription.getPrescriptionCode());
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
        log.debug("Getting active batches for drugId={}", drugId);
        List<Inventory> inventories = inventoryRepository.findActiveBatchesByDrugId(drugId);
        log.debug("Query returned {} inventory records for drugId={}", inventories.size(), drugId);
        return inventories.stream()
            .map(inv -> convertToDrugBatchDTOWithStock(inv.getBatch(), inv))
            .collect(Collectors.toList());
    }

    // ========================== BÁO CÁO ==========================

    @Override
    public List<InventoryLog> getInventoryLog(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return inventoryLogRepository.findByPerformedAtBetween(start, end);
    }

    @Override
    public Page<InventoryLog> getInventoryLog(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return inventoryLogRepository.findByPerformedAtBetween(start, end, pageable);
    }

    // ========================== HÀM HỖ TRỢ NỘI BỘ ==========================


    private DrugDTO convertToDrugDTO(Drug drug) {
        List<Inventory> inventories = inventoryRepository.findByDrugId(drug.getDrugId());
        int totalQty = inventories.stream().mapToInt(Inventory::getQuantityInStock).sum();

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
            .manufacturer(drug.getManufacturer())
            .countryOfOrigin(drug.getCountryOfOrigin())
            .storageCondition(drug.getStorageCondition())
            .notes(drug.getNotes())
            .status(drug.getStatus())
            .totalQuantityInStock(totalQty)
            .totalBatches(drug.getBatches() != null ? drug.getBatches().size() : 0)
            .createdByName(drug.getCreatedByUser() != null ? drug.getCreatedByUser().getFullName() : null)
            .createdAt(drug.getCreatedAt() != null ? drug.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null)
            .updatedBy(drug.getUpdatedByUser() != null ? drug.getUpdatedByUser().getUserId() : null)
            .updatedByName(drug.getUpdatedByUser() != null ? drug.getUpdatedByUser().getFullName() : null)
            .updatedAt(drug.getUpdatedAt() != null ? drug.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null)

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
            .unitName(getSanitizedUnitName(batch.getUnit()))
            .unitId(batch.getUnit().getUnitId())
            .quantity(batch.getQuantity())
            .supplier(batch.getSupplier())
            .importDate(batch.getImportDate())
            .importedBy(batch.getImportedByUser().getFullName())
            .status(batch.getStatus())
            .drugStatus(batch.getDrug().getStatus())
            .notes(batch.getNotes())
            .updatedBy(batch.getUpdatedByUser() != null ? batch.getUpdatedByUser().getFullName() : null)
            .updatedAt(batch.getUpdatedAt())
            .updateReason(batch.getUpdateReason())
            .quantityInStock(inv != null ? inv.getQuantityInStock() : 0)
            .daysUntilExpiry(daysUntilExpiry)
            .build();
    }

    private InventoryDTO convertToInventoryDTO(Inventory inventory) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), inventory.getBatch().getExpiryDate());
        boolean isExpiringSoon = daysUntilExpiry <= 7 && daysUntilExpiry > 0;
        boolean isLowStock = inventory.getQuantityInStock() < 50;

        int factor = calculateSmallUnitQuantity(inventory.getBatch().getDrug().getDrugId(), inventory.getBatch().getUnit().getUnitId(), 1);
        String largeUnitName = getSanitizedUnitName(inventory.getBatch().getUnit());
        String smallUnitName = getSanitizedUnitName(inventory.getBatch().getDrug().getBaseUnit());

        int quantityInLargeUnit = factor > 0 ? inventory.getQuantityInStock() / factor : 0;

        return InventoryDTO.builder()
            .inventoryId(inventory.getInventoryId())
            .batchId(inventory.getBatch().getBatchId())
            .batchNumber(inventory.getBatch().getBatchNumber())
            .drugId(inventory.getBatch().getDrug().getDrugId())
            .drugName(inventory.getBatch().getDrug().getDrugName())
            .quantityInStock(inventory.getQuantityInStock())
            .lastUpdated(inventory.getLastUpdated())
            .daysUntilExpiry(daysUntilExpiry)
            .isExpiringSoon(isExpiringSoon)
            .isLowStock(isLowStock)
            .drugCode(inventory.getBatch().getDrug().getDrugCode())
            .expiryDate(inventory.getBatch().getExpiryDate())
            .quantityInLargeUnit(quantityInLargeUnit)
            .largeUnitName(largeUnitName)
            .smallUnitName(smallUnitName)
            .build();
    }

    @Override
    public List<Unit> getAllUnits() {
        List<Unit> units = unitRepository.findAll();
        for (Unit u : units) {
            u.setUnitName(getSanitizedUnitName(u));
        }
        return units;
    }

    private DrugConversionDTO convertToDrugConversionDTO(UnitConversion conversion) {
        return DrugConversionDTO.builder()
            .largeUnitId(conversion.getLargeUnit() != null ? conversion.getLargeUnit().getUnitId() : null)
            .largeUnitName(conversion.getLargeUnit() != null ? getSanitizedUnitName(conversion.getLargeUnit()) : "Đơn vị")
            .smallUnitId(conversion.getSmallUnit() != null ? conversion.getSmallUnit().getUnitId() : null)
            .smallUnitName(conversion.getSmallUnit() != null ? getSanitizedUnitName(conversion.getSmallUnit()) : "Đơn vị")
            .conversionQuantity(conversion.getConversionQuantity())
            .build();
    }

    private String getSanitizedUnitName(Unit unit) {
        if (unit == null) return "Đơn vị";
        String name = unit.getUnitName();
        if (name == null || name.trim().isEmpty()) return "Đơn vị";
        
        name = name.trim().toUpperCase();
        if (name.equals("VIÊN") || name.equals("VIEN")) return "VIÊN";
        if (name.equals("ỐNG") || name.equals("ONG") || name.contains("?NG") || name.equals("?NG")) return "ỐNG";
        if (name.equals("LỌ") || name.equals("LO") || name.startsWith("L?")) return "LỌ";
        if (name.equals("VỈ") || name.equals("VI") || name.startsWith("V?")) return "VỈ";
        if (name.equals("HỘP") || name.equals("HOP") || name.contains("H?P") || name.contains("H?P")) return "HỘP";
        if (name.equals("TUÝP") || name.equals("TUYP")) return "TUÝP";
        
        return name;
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
            .dispensedByUserId(detail.getDispensedByUser() != null ? detail.getDispensedByUser().getUserId() : null)
            .notes(detail.getNotes())
            .patientId(detail.getPrescription().getPatient().getPatientId())
            .patientName(detail.getPrescription().getPatient().getUser().getFullName())
            .patientCccd(detail.getPrescription().getPatient().getUser().getNationalID())
            .patientDob(detail.getPrescription().getPatient().getDob())
            .isPending(detail.getStatus() == null || detail.getStatus() == PrescriptionDetailStatus.PENDING)
            .status(detail.getStatus() != null ? detail.getStatus() : (detail.getQuantityDispensed() != null && detail.getQuantityDispensed() > 0 ? PrescriptionDetailStatus.DISPENSED : PrescriptionDetailStatus.PENDING))
            .build();
    }
}
