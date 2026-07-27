package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.InitiateCreateStaffRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingStaffRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import com.mycompany.jpademo.backend.util.OtpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycompany.jpademo.backend.entity.User;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Author: GiangLTHE194888
 * Task: Manages administration tasks, user management, doctor account creation with OTP verification, and system log audits.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final SystemLogService systemLogService;

    /** Displays the admin dashboard statistics and logs. */
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

    /** Displays a paginated, filterable list of all system users. */
    @GetMapping("/users")
    public String userManagement(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

            Page<UserResponse> users = adminService.getUser(keyword, role, status, startDateTime, endDateTime, pageable);
            List<String> roles = adminService.getRoleName();
            List<String> userStatus = adminService.getUserStatus();
            model.addAttribute("users", users);
            model.addAttribute("keyword", keyword);
            model.addAttribute("role", role);
            model.addAttribute("status", status);
            model.addAttribute("roles", roles);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("statuses", userStatus);
            return "admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /** Retrieves details of a specific user. */
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

    /** Updates a user's status — either ACTIVE (unban) or BANNED (ban). */
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
                redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công, nhưng gửi email thông báo cho người dùng đã thất bại.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    /** Renders the step-based doctor registration page. */
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

    /** Handles step 1 of doctor creation, submitting details and sending OTP. */
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

    /** Renders the OTP verification page for doctor creation. */
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

    /** Verifies the OTP and finalizes the creation of the doctor user. */
    @PostMapping("/create-staff/confirm")
    public String confirmCreateStaff(
            @Valid @ModelAttribute VerifyPendingStaffRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User adminUser = userDetails.getUser();
            adminService.verifyAndCreateStaff(request, adminUser);
            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản nội bộ thành công!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("requestId", request.getRequestId());
            return "redirect:/admin/create-staff/verify";
        }
    }

    /** Resends the registration OTP code to the administrator. */
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

    /** Displays the paginated and filterable system logs list. */
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

    /** Retrieves and displays details of a specific system log entry. */
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

    /** Downloads or views a doctor's certificate file. */
    @GetMapping("/users/{id}/certificate")
    public ResponseEntity<Resource> viewStaffCertificate(
            @PathVariable Integer id,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            CertificateFileResponse certificate = adminService.getStaffCertificate(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + certificate.getDisplayName() + "\"")
                    .contentType(certificate.getMediaType())
                    .body(certificate.getResource());
        } catch (BadRequestException | ResourceNotFoundException e) {
            String targetPath = "/admin/users/" + id;

            FlashMap flashMap = new FlashMap();
            flashMap.put("error", e.getMessage());
            flashMap.setTargetRequestPath(targetPath);

            FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
            flashMapManager.saveOutputFlashMap(flashMap, request, response);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(targetPath))
                    .build();
        }
    }
}