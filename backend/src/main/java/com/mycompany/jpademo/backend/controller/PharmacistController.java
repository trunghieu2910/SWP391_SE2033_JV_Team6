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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/pharmacist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACIST')")
public class PharmacistController {
    private final PharmacistService pharmacistService;
    private final ProfileService profileService;

    // ==================== DASHBOARD ====================
    @GetMapping({"/", "/dashboard"})
    public String dashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        try {
            PharmacistDashboardDTO stats = pharmacistService.getDashboardStats();
            model.addAttribute("stats", stats);
            model.addAttribute("userName", userDetails.getUser().getFullName());
            return "pharmacist/dashboard";
        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Lỗi khi tải bảng điều khiển");
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
            model.addAttribute("error", "Lỗi khi tải danh sách thuốc");
            return "pharmacist/drug-list";
        }
    }

    @GetMapping("/drug-detail/{drugId}")
    public String getDrugDetail(
            @PathVariable Integer drugId,
            Model model) {
        try {
            DrugDTO drug = pharmacistService.getDrugDetail(drugId);
            List<DrugBatchDTO> batches = pharmacistService.getBatchesByDrug(drugId);
            model.addAttribute("drug", drug);
            model.addAttribute("batches", batches);
            return "pharmacist/drug-detail";
        } catch (Exception e) {
            log.error("Error loading drug detail", e);
            model.addAttribute("error", "Lỗi khi tải chi tiết thuốc");
            return "pharmacist/drug-list";
        }
    }

    @GetMapping("/drug-add")
    public String showAddDrugForm(Model model) {
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
            model.addAttribute("error", "Lỗi khi tải lịch sử nhập kho");
            return "pharmacist/import-history";
        }
    }

    @GetMapping("/drug-import")
    public String showImportForm(Model model) {
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

    // ==================== DISPENSING MANAGEMENT ====================
    @GetMapping("/dispense-list")
    public String getDispenseList(
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        try {
            Page<PrescriptionDetailDTO> prescriptions = pharmacistService.getPendingPrescriptions(pageable);
            model.addAttribute("prescriptions", prescriptions);
            model.addAttribute("currentPage", pageable.getPageNumber());
            model.addAttribute("totalPages", prescriptions.getTotalPages());
            return "pharmacist/dispense-list";
        } catch (Exception e) {
            log.error("Error loading dispense list", e);
            model.addAttribute("error", "Lỗi khi tải danh sách cấp phát");
            return "pharmacist/dispense-list";
        }
    }

    @GetMapping("/dispense-form/{detailId}")
    public String showDispenseForm(
            @PathVariable Integer detailId,
            Model model) {
        try {
            PrescriptionDetailDTO detail = pharmacistService.getPrescriptionDetail(detailId);
            model.addAttribute("detail", detail);
            model.addAttribute("dispenseForm", new DispenseDrugRequest());
            return "pharmacist/dispense-form";
        } catch (Exception e) {
            log.error("Error loading dispense form", e);
            model.addAttribute("error", "Lỗi khi tải form cấp phát");
            return "pharmacist/dispense-list";
        }
    }

    @PostMapping("/drug-dispense")
    public String dispenseDrug(
            @ModelAttribute DispenseDrugRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            pharmacistService.dispenseDrug(request, userDetails.getUser().getUserId());
            redirectAttributes.addFlashAttribute("success", "Cấp phát thuốc thành công");
            return "redirect:/pharmacist/dispense-list";
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
            model.addAttribute("error", "Lỗi khi tải tồn kho");
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
            model.addAttribute("error", "Lỗi khi tải báo cáo");
            return "pharmacist/reports";
        }
    }

    @GetMapping("/export-report")
    @ResponseBody
    public ResponseEntity<?> exportReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<InventoryLog> logs = pharmacistService.getInventoryLog(startDate, endDate);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error exporting report", e);
            return ResponseEntity.status(400).body(java.util.Map.of("error", e.getMessage()));
        }
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
            List<DrugBatchDTO> batches = pharmacistService.getActiveBatchesForDrug(drugId);
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

