package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.InitiateCreateStaffRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingStaffRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import com.mycompany.jpademo.backend.util.OtpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycompany.jpademo.backend.entity.User;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final SystemLogService systemLogService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        String adminEmail = userDetails.getUser().getEmail();
        DashboardPageResponse data = adminService.getDashboardPageData(startDate, endDate);

        model.addAttribute("stats", data.getStats());
        model.addAttribute("charts", data.getCharts());
        model.addAttribute("recentLogs", data.getRecentLogs());
        model.addAttribute("startDate", data.getStartDate());
        model.addAttribute("endDate", data.getEndDate());
        model.addAttribute("adminEmail", adminEmail);
        if (data.getErrorMessage() != null) {
            model.addAttribute("error", data.getErrorMessage());
        }
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

        Page<UserResponse> users = adminService.getUser(keyword, role, status, startDateTime, endDateTime, pageable);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("roles", new String[]{"", "ADMIN", "DOCTOR", "PATIENT"});
        model.addAttribute("statuses", new String[]{"", "ACTIVE", "INACTIVE", "BANNED"});
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UserDetailResponse userDetail = adminService.getUserDetail(id);
            model.addAttribute("user", userDetail);
            return "admin/user-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{id}/status")
    public String updateUserStatus(
            @PathVariable Integer id,
            @Valid @ModelAttribute UpdateUserStatusRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        request.setUserId(id);
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/admin/users/" + id;
        }
        try {
            User adminUser = userDetails.getUser();
            boolean isUpdated = adminService.updateUserStatus(request, adminUser);
            if (isUpdated) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái. Vui lòng kiểm tra lại.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @GetMapping("/create-staff")
    public String createStaffPage(
            @RequestParam(defaultValue = "1") int step,
            @RequestParam(required = false) Integer remainingTime,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        if (!model.containsAttribute("staffRequest")) {
            model.addAttribute("staffRequest", new InitiateCreateStaffRequest());
        }

        String adminEmail = userDetails.getUser().getEmail();

        int remainingTimeValue;
        if (remainingTime != null) {
            remainingTimeValue = remainingTime;
        } else {
            remainingTimeValue = OtpUtil.getRemainingTime(adminEmail);
        }
        model.addAttribute("step", step);
        model.addAttribute("remainingTime", remainingTimeValue);
        model.addAttribute("adminEmail", adminEmail);
        return "admin/create-staff";
    }

    @PostMapping("/create-staff/initiate")
    public String initiateCreateStaff(
            @Valid @ModelAttribute("staffRequest") InitiateCreateStaffRequest request,
            BindingResult result,
            @RequestParam(required = false) MultipartFile certificateFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("staffRequest", request);
            redirectAttributes.addFlashAttribute("hasErrors", true);
            redirectAttributes.addFlashAttribute("errors", result.getAllErrors());
            return "redirect:/admin/create-staff";
        }
        try {
            if (certificateFile != null && !certificateFile.isEmpty()) {
                request.setCertificateFile(certificateFile);
            }
            User adminUser = userDetails.getUser();
            InitiateCreateStaffResponse response = adminService.initiateCreateStaff(request, adminUser);
            redirectAttributes.addAttribute("requestId", response.getRequestId());
            return "redirect:/admin/create-staff/verify";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("staffRequest", request);
            return "redirect:/admin/create-staff";
        }
    }

    @GetMapping("/create-staff/verify")
    public String verifyStaffPage(
            @RequestParam(required = false) String requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        String adminEmail = userDetails.getUser().getEmail();
        int remainingTime = OtpUtil.getRemainingTime(adminEmail);
        model.addAttribute("step", 2);
        model.addAttribute("requestId", requestId);
        model.addAttribute("remainingTime", remainingTime);
        model.addAttribute("adminEmail", adminEmail);
        return "admin/create-staff";
    }

    @PostMapping("/create-staff/confirm")
    public String confirmCreateStaff(
            @Valid @ModelAttribute VerifyPendingStaffRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User adminUser = userDetails.getUser();
            adminService.verifyAndCreateStaff(request, adminUser);
            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản bác sĩ thành công!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("requestId", request.getRequestId());
            return "redirect:/admin/create-staff/verify";
        }
    }

    @PostMapping("/create-staff/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendOtp(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String adminEmail = userDetails.getUser().getEmail();
            return ResponseEntity.ok(adminService.resendOtp(adminEmail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/logs")
    public String systemLogs(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(page = 0, size = 10, sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        LocalDateTime startLogs = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endLogs = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;
        Page<SystemLogResponse> logs = systemLogService.getLogs(action, keyword, startLogs, endLogs, pageable);
        model.addAttribute("logs", logs);
        model.addAttribute("userId", userId);
        model.addAttribute("action", action);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/logs";
    }

    @GetMapping("/logs/{id}")
    public String logDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            SystemLogResponse log = systemLogService.getLogDetail(id);
            model.addAttribute("log", log);
            return "admin/log-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể tải chi tiết nhật kí này");
            return "redirect:/admin/logs";
        }
    }

    @GetMapping("/users/{id}/certificate")
    public ResponseEntity<Resource> viewDoctorCertificate(@PathVariable Integer id) {
        CertificateFileResponse certificate = adminService.getDoctorCertificate(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + certificate.getDisplayName() + "\"")
                .contentType(certificate.getMediaType())
                .body(certificate.getResource());
    }
}
