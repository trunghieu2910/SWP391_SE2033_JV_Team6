package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreateDrugRequest;
import com.mycompany.jpademo.backend.dto.request.DispenseDrugRequest;
import com.mycompany.jpademo.backend.dto.request.ImportDrugBatchRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.InventoryLog;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.PharmacistService;
import com.mycompany.jpademo.backend.service.interfaces.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

// OpenPDF imports
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Slf4j
@Controller
@RequestMapping("/pharmacist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACIST')")
public class PharmacistController {
    private final PharmacistService pharmacistService;
    private final ProfileService profileService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        try {
            if (startDate == null) startDate = LocalDate.now().minusMonths(1);
            if (endDate == null) endDate = LocalDate.now();

            int pageSize = 10;
            Pageable pageable = PageRequest.of(page, pageSize);

            PharmacistDashboardDTO stats = pharmacistService.getDashboardStats();
            Page<InventoryLog> logsPage = pharmacistService.getInventoryLog(startDate, endDate, pageable);

            model.addAttribute("stats", stats);
            model.addAttribute("logs", logsPage.getContent());
            model.addAttribute("currentPage", logsPage.getNumber());
            model.addAttribute("totalPages", logsPage.getTotalPages());
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("userName", userDetails.getUser().getFullName());
            return "pharmacist/dashboard";
        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Lỗi khi tải trang chủ: " + e.getMessage());
            model.addAttribute("logs", Collections.emptyList());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("startDate", startDate != null ? startDate : LocalDate.now().minusMonths(1));
            model.addAttribute("endDate", endDate != null ? endDate : LocalDate.now());
            return "pharmacist/dashboard";
        }
    }

    // ==================== DRUG MANAGEMENT ====================
    @GetMapping("/drug-list")
    public String getDrugList(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer subCategoryId,
            Model model) {
        try {
            Page<DrugDTO> drugs;
            if (search != null && !search.isEmpty()) {
                drugs = pharmacistService.searchDrugs(search, pageable);
                model.addAttribute("search", search);
            } else if (subCategoryId != null) {
                drugs = pharmacistService.getDrugsByCategory(subCategoryId, pageable);
                model.addAttribute("subCategoryId", subCategoryId);
            } else {
                drugs = pharmacistService.getDrugList(pageable);
            }
            
            model.addAttribute("drugs", drugs);
            model.addAttribute("currentPage", pageable.getPageNumber());
            model.addAttribute("totalPages", drugs.getTotalPages());
            return "pharmacist/drug-list";
        } catch (Exception e) {
            log.error("Error loading drug list", e);
            model.addAttribute("error", "Lỗi khi tải danh sách thuốc: " + e.getMessage());
            model.addAttribute("drugs", Page.empty());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            return "pharmacist/drug-list";
        }
    }

    @GetMapping("/drug-detail/{drugId}")
    public String getDrugDetail(
            @PathVariable Integer drugId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            DrugDTO drug = pharmacistService.getDrugDetail(drugId);
            List<DrugBatchDTO> batches = pharmacistService.getBatchesByDrug(drugId);
            model.addAttribute("drug", drug);
            model.addAttribute("batches", batches);
            return "pharmacist/drug-detail";
        } catch (Exception e) {
            log.error("Error loading drug detail", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải chi tiết thuốc: " + e.getMessage());
            return "redirect:/pharmacist/drug-list";
        }
    }

    @GetMapping("/drug-add")
    public String showAddDrugForm(Model model) {
        String nextDrugCode = pharmacistService.generateNextDrugCode();
        model.addAttribute("nextDrugCode", nextDrugCode);
        model.addAttribute("drugForm", new CreateDrugRequest());
        return "pharmacist/drug-form";
    }

    @PostMapping("/drug-add")
    public String addDrug(
            @ModelAttribute CreateDrugRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            pharmacistService.createDrug(request, userDetails.getUser().getUserId());
            redirectAttributes.addFlashAttribute("success", "Thêm thuốc thành công");
            return "redirect:/pharmacist/drug-list";
        } catch (Exception e) {
            log.error("Error adding drug", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm thuốc: " + e.getMessage());
            return "redirect:/pharmacist/drug-add";
        }
    }

    @PostMapping("/drug-update/{drugId}")
    public String updateDrug(
            @PathVariable Integer drugId,
            @ModelAttribute CreateDrugRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            pharmacistService.updateDrug(drugId, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thuốc thành công");
            return "redirect:/pharmacist/drug-detail/" + drugId;
        } catch (Exception e) {
            log.error("Error updating drug", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật thuốc");
            return "redirect:/pharmacist/drug-detail/" + drugId;
        }
    }

    @PostMapping("/drug-status/{drugId}")
    public ResponseEntity<?> updateDrugStatus(
            @PathVariable Integer drugId,
            @RequestParam Byte status) {
        try {
            pharmacistService.updateDrugStatus(drugId, status);
            return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Cập nhật trạng thái thành công"));
        } catch (Exception e) {
            log.error("Error updating drug status", e);
            return ResponseEntity.status(400).body(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== IMPORT MANAGEMENT ====================
    @GetMapping("/import-history")
    public String getImportHistory(
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        try {
            Page<DrugBatchDTO> batches = pharmacistService.getDrugBatches(pageable);
            model.addAttribute("batches", batches);
            model.addAttribute("currentPage", pageable.getPageNumber());
            model.addAttribute("totalPages", batches.getTotalPages());
            return "pharmacist/import-history";
        } catch (Exception e) {
            log.error("Error loading import history", e);
            model.addAttribute("error", "Lỗi khi tải lịch sử nhập kho: " + e.getMessage());
            model.addAttribute("batches", Page.empty());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            return "pharmacist/import-history";
        }
    }

    @GetMapping("/drug-import")
    public String showImportForm(Model model) {
        model.addAttribute("nextBatchStt", pharmacistService.generateNextBatchStt());
        model.addAttribute("drugs", pharmacistService.getAllActiveDrugs());
        model.addAttribute("units", pharmacistService.getAllUnits());
        model.addAttribute("importForm", new ImportDrugBatchRequest());
        return "pharmacist/drug-import";
    }

    @PostMapping("/drug-import")
    public String importDrug(
            @ModelAttribute ImportDrugBatchRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            pharmacistService.createDrugBatch(request, userDetails.getUser().getUserId());
            redirectAttributes.addFlashAttribute("success", "Nhập kho thành công");
            return "redirect:/pharmacist/import-history";
        } catch (Exception e) {
            log.error("Error importing drug", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi nhập kho: " + e.getMessage());
            return "redirect:/pharmacist/drug-import";
        }
    }

    @GetMapping("/batch-detail/{batchId}")
    public String showBatchDetail(
            @PathVariable Integer batchId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            DrugBatchDTO batch = pharmacistService.getDrugBatchDetail(batchId);
            model.addAttribute("batch", batch);
            model.addAttribute("units", pharmacistService.getAllUnits());
            model.addAttribute("updateForm", ImportDrugBatchRequest.builder()
                .manufactureDate(batch.getManufactureDate())
                .expiryDate(batch.getExpiryDate())
                .unitId(batch.getUnitId())
                .quantity(batch.getQuantity())
                .importPrice(batch.getImportPrice())
                .supplier(batch.getSupplier())
                .notes(batch.getNotes())
                .build());
            return "pharmacist/batch-detail";
        } catch (Exception e) {
            log.error("Error loading batch detail", e);
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy lô hàng: " + e.getMessage());
            return "redirect:/pharmacist/import-history";
        }
    }

    @PostMapping("/batch-update/{batchId}")
    public String updateBatch(
            @PathVariable Integer batchId,
            @ModelAttribute ImportDrugBatchRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            pharmacistService.updateDrugBatch(batchId, request, userDetails.getUser().getUserId());
            redirectAttributes.addFlashAttribute("success", "Cập nhật lô hàng thành công");
            return "redirect:/pharmacist/batch-detail/" + batchId;
        } catch (Exception e) {
            log.error("Error updating batch", e);
            redirectAttributes.addFlashAttribute("error", "Cập nhật lô hàng thất bại: " + e.getMessage());
            return "redirect:/pharmacist/batch-detail/" + batchId;
        }
    }

    // ==================== DISPENSING MANAGEMENT ====================
    @GetMapping("/dispense-list")
    public String getDispenseList(Model model) {
        try {
            List<PrescriptionSummaryDTO> prescriptions = pharmacistService.getPendingPrescriptionsSummary();
            model.addAttribute("prescriptions", prescriptions);
            return "pharmacist/dispense-list";
        } catch (Exception e) {
            log.error("Error loading dispense list", e);
            model.addAttribute("error", "Lỗi khi tải danh sách cấp phát: " + e.getMessage());
            model.addAttribute("prescriptions", java.util.Collections.emptyList());
            return "pharmacist/dispense-list";
        }
    }

    @GetMapping("/dispense-prescription/{prescriptionId}")
    public String getDispensePrescriptionDetail(
            @PathVariable Integer prescriptionId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<PrescriptionDetailDTO> details = pharmacistService.getPrescriptionDetailsByPrescriptionId(prescriptionId);
            if (details.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn thuốc");
                return "redirect:/pharmacist/dispense-list";
            }
            PrescriptionDetailDTO first = details.get(0);
            model.addAttribute("prescriptionCode", first.getPrescriptionCode());
            model.addAttribute("patientName", first.getPatientName());
            model.addAttribute("prescriptionId", prescriptionId);
            model.addAttribute("details", details);
            return "pharmacist/dispense-prescription-detail";
        } catch (Exception e) {
            log.error("Error loading prescription detail", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải chi tiết đơn thuốc: " + e.getMessage());
            return "redirect:/pharmacist/dispense-list";
        }
    }

    @GetMapping("/dispense-form/{detailId}")
    public String showDispenseForm(
            @PathVariable Integer detailId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            PrescriptionDetailDTO detail = pharmacistService.getPrescriptionDetail(detailId);
            model.addAttribute("detail", detail);
            model.addAttribute("dispenseForm", new DispenseDrugRequest());
            return "pharmacist/dispense-form";
        } catch (Exception e) {
            log.error("Error loading dispense form", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải form cấp phát: " + e.getMessage());
            return "redirect:/pharmacist/dispense-list";
        }
    }

    @PostMapping("/drug-dispense")
    public String dispenseDrug(
            @ModelAttribute DispenseDrugRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        Integer prescriptionId = null;
        try {
            PrescriptionDetailDTO detail = pharmacistService.getPrescriptionDetail(request.getDetailId());
            prescriptionId = detail.getPrescriptionId();
            pharmacistService.dispenseDrug(request, userDetails.getUser().getUserId());
            redirectAttributes.addFlashAttribute("success", "Cấp phát thuốc thành công");
            return "redirect:/pharmacist/dispense-prescription/" + prescriptionId;
        } catch (Exception e) {
            log.error("Error dispensing drug", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cấp phát thuốc: " + e.getMessage());
            return "redirect:/pharmacist/dispense-form/" + request.getDetailId();
        }
    }

    // ==================== INVENTORY MANAGEMENT ====================
    @GetMapping("/inventory")
    public String getInventory(
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        try {
            Page<InventoryDTO> inventories = pharmacistService.getInventory(pageable);
            List<InventoryDTO> expiringItems = pharmacistService.getExpiringInventory();
            List<InventoryDTO> lowStockItems = pharmacistService.getLowStockInventory();
            
            model.addAttribute("inventories", inventories);
            model.addAttribute("expiringItems", expiringItems);
            model.addAttribute("lowStockItems", lowStockItems);
            model.addAttribute("currentPage", pageable.getPageNumber());
            model.addAttribute("totalPages", inventories.getTotalPages());
            return "pharmacist/inventory";
        } catch (Exception e) {
            log.error("Error loading inventory", e);
            model.addAttribute("error", "Lỗi khi tải tồn kho: " + e.getMessage());
            model.addAttribute("inventories", Page.empty());
            model.addAttribute("expiringItems", Collections.emptyList());
            model.addAttribute("lowStockItems", Collections.emptyList());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            return "pharmacist/inventory";
        }
    }

    @PostMapping("/inventory-adjust/{batchId}")
    public ResponseEntity<?> adjustInventory(
            @PathVariable Integer batchId,
            @RequestParam Integer quantityChange,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            pharmacistService.adjustInventory(batchId, quantityChange, reason, userDetails.getUser().getUserId());
            return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Điều chỉnh tồn kho thành công"));
        } catch (Exception e) {
            log.error("Error adjusting inventory", e);
            return ResponseEntity.status(400).body(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== REPORTS ====================
    @GetMapping("/reports")
    public String getReports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        try {
            if (startDate == null) startDate = LocalDate.now().minusMonths(1);
            if (endDate == null) endDate = LocalDate.now();
            
            List<InventoryLog> logs = pharmacistService.getInventoryLog(startDate, endDate);
            List<InventoryDTO> expiringBatches = pharmacistService.getExpiringInventory();
            
            model.addAttribute("logs", logs);
            model.addAttribute("expiringBatches", expiringBatches);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            return "pharmacist/reports";
        } catch (Exception e) {
            log.error("Error loading reports", e);
            model.addAttribute("error", "Lỗi khi tải báo cáo: " + e.getMessage());
            model.addAttribute("logs", Collections.emptyList());
            model.addAttribute("expiringBatches", Collections.emptyList());
            model.addAttribute("startDate", startDate != null ? startDate : LocalDate.now().minusMonths(1));
            model.addAttribute("endDate", endDate != null ? endDate : LocalDate.now());
            return "pharmacist/reports";
        }
    }

    @GetMapping("/export-report")
    public void exportReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"bao-cao-kho-" + startDate + "-den-" + endDate + ".pdf\"");

        List<InventoryLog> logs = pharmacistService.getInventoryLog(startDate, endDate);

        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Set up fonts for Vietnamese (Unicode) using Tahoma or Arial from Windows
            Font fontTitle;
            Font fontSub;
            Font fontHeader;
            Font fontBody;
            try {
                BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\tahoma.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                fontTitle = new Font(bf, 16, Font.BOLD, java.awt.Color.DARK_GRAY);
                fontSub = new Font(bf, 10, Font.ITALIC, java.awt.Color.GRAY);
                fontHeader = new Font(bf, 9, Font.BOLD, java.awt.Color.WHITE);
                fontBody = new Font(bf, 8, Font.NORMAL, java.awt.Color.BLACK);
            } catch (Exception e) {
                // Fallback to standard Helvetica if font file not found
                fontTitle = new Font(Font.HELVETICA, 16, Font.BOLD);
                fontSub = new Font(Font.HELVETICA, 10, Font.ITALIC);
                fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD);
                fontBody = new Font(Font.HELVETICA, 8, Font.NORMAL);
            }

            // Title
            Paragraph title = new Paragraph("BÁO CÁO NHẬT KÝ HOẠT ĐỘNG KHO", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            // Subtitle / Date range
            Paragraph subtitle = new Paragraph("Từ ngày: " + startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                                " - Đến ngày: " + endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontSub);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Create Table
            PdfPTable table = new PdfPTable(8); // 8 columns
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2.0f, 1.2f, 1.0f, 1.0f, 1.0f, 1.5f, 1.8f});

            // Headers
            String[] headers = {"Ngày thực hiện", "Thuốc", "Hoạt động", "Thay đổi", "Tồn trước", "Tồn sau", "Dược sĩ", "Ghi chú"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                cell.setBackgroundColor(new java.awt.Color(15, 118, 110)); // #0f766e - MedAI Teal
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            // Rows
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (InventoryLog log : logs) {
                // 1. Date
                table.addCell(createPdfCell(log.getPerformedAt() != null ? log.getPerformedAt().format(dtf) : "-", fontBody, Element.ALIGN_CENTER));
                
                // 2. Drug
                table.addCell(createPdfCell(log.getBatch().getDrug().getDrugName(), fontBody, Element.ALIGN_LEFT));
                
                // 3. Action type
                String action = switch (log.getActionType()) {
                    case "IMPORT" -> "Nhập kho";
                    case "DISPENSE" -> "Xuất đơn";
                    case "ADJUST" -> "Cân đối";
                    default -> log.getActionType();
                };
                table.addCell(createPdfCell(action, fontBody, Element.ALIGN_CENTER));
                
                // 4. Change
                String change = (log.getQuantityChange() > 0 ? "+" : "") + log.getQuantityChange();
                table.addCell(createPdfCell(change, fontBody, Element.ALIGN_RIGHT));
                
                // 5. Before
                table.addCell(createPdfCell(String.valueOf(log.getQuantityBefore()), fontBody, Element.ALIGN_RIGHT));
                
                // 6. After
                table.addCell(createPdfCell(String.valueOf(log.getQuantityAfter()), fontBody, Element.ALIGN_RIGHT));
                
                // 7. Pharmacist
                table.addCell(createPdfCell(log.getUser().getFullName(), fontBody, Element.ALIGN_LEFT));
                
                // 8. Notes
                table.addCell(createPdfCell(log.getNotes() != null ? log.getNotes() : "-", fontBody, Element.ALIGN_LEFT));
            }

            document.add(table);
        } catch (DocumentException e) {
            throw new IOException("Error writing PDF document", e);
        }
    }

    private PdfPCell createPdfCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    // ==================== REST API FOR AJAX ====================

    /**
     * API tra ve danh sach lo hang con hang cho mot thuoc.
     * Dung boi dispense-form.js de populate dropdown chon lo hang.
     * GET /pharmacist/api/batches?drugId=1
     */
    @GetMapping("/api/batches")
    @ResponseBody
    public ResponseEntity<?> getActiveBatchesForDrug(@RequestParam Integer drugId) {
        try {
            log.info("Fetching active batches for drugId={}", drugId);
            List<DrugBatchDTO> batches = pharmacistService.getActiveBatchesForDrug(drugId);
            log.info("Found {} active batches for drugId={}", batches != null ? batches.size() : 0, drugId);
            return ResponseEntity.ok(batches);
        } catch (Exception e) {
            log.error("Error fetching active batches for drugId={}", drugId, e);
            return ResponseEntity.status(400).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // ==================== PROFILE ====================
    @GetMapping("/profile")
    public String profilePage(Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "success", required = false) boolean success) {
        try {
            ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
            UpdateProfileRequest profileForm = UpdateProfileRequest.builder()
                    .username(profile.getUsername())
                    .fullName(profile.getFullName())
                    .phoneNumber(profile.getPhoneNumber())
                    .nationalID(profile.getNationalID())
                    .gender(profile.getGender())
                    .dob(profile.getDob())
                    .address(profile.getAddress())
                    .build();

            model.addAttribute("profile", profile);
            model.addAttribute("profileForm", profileForm);
            model.addAttribute("success", success);
            model.addAttribute("today", LocalDate.now());
            return "pharmacist/profile";
        } catch (Exception e) {
            log.error("Error loading pharmacist profile", e);
            model.addAttribute("error", "Lỗi khi tải thông tin hồ sơ");
            return "pharmacist/dashboard";
        }
    }

    @PostMapping("/profile")
    public String saveProfile(@Valid @ModelAttribute("profileForm") UpdateProfileRequest profileForm,
                               BindingResult bindingResult,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
            if (bindingResult.hasErrors()) {
                model.addAttribute("profile", profile);
                model.addAttribute("profileForm", profileForm);
                return "pharmacist/profile";
            }

            profileService.updateProfile(userDetails.getUsername(), profileForm);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công.");
            return "redirect:/pharmacist/profile?success=true";
        } catch (Exception e) {
            log.error("Error saving pharmacist profile", e);
            model.addAttribute("error", "Lỗi khi cập nhật hồ sơ: " + e.getMessage());
            return "pharmacist/profile";
        }
    }
}

