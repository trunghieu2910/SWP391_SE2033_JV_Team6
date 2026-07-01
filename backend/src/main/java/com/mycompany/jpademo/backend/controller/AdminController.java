package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.InitiateCreateDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingDoctorRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.DuplicateResourceException;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import com.mycompany.jpademo.backend.util.OtpUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.entity.User;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final SystemLogService systemLogService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        try {
            // Nếu không có filter, mặc định 6 tháng gần nhất
            if (startDate == null && endDate == null) {
                endDate = LocalDate.now();
                startDate = endDate.minusMonths(6);
            }

            // Nếu chỉ có startDate, set endDate = startDate + 1 tháng
            if (startDate != null && endDate == null) {
                endDate = startDate.plusMonths(1);
            }

            // Nếu chỉ có endDate, set startDate = endDate - 6 tháng
            if (endDate != null && startDate == null) {
                startDate = endDate.minusMonths(6);
            }

            // Đảm bảo startDate <= endDate
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                LocalDate temp = startDate;
                startDate = endDate;
                endDate = temp;
            }
            DashboardStatsResponse stats = adminService.getDashboardStats(startDate, endDate);
            ChartStatsResponse charts = adminService.getChartStats(startDate, endDate);
            if (stats == null) {
                stats = DashboardStatsResponse.builder()
                        .totalUsers(0L)
                        .totalDoctors(0L)
                        .totalPatients(0L)
                        .blockedUsers(0L)
                        .totalDiagnosisSessions(0L)
                        .build();
            }
            if (charts == null) {
                charts = ChartStatsResponse.builder()
                        .userRegistrations(new ArrayList<>())
                        .diagnosisSessions(new ArrayList<>())
                        .build();
            }
            if (charts.getUserRegistrations() == null) {
                charts = ChartStatsResponse.builder()
                        .userRegistrations(new ArrayList<>())
                        .diagnosisSessions(charts.getDiagnosisSessions() != null ? charts.getDiagnosisSessions() : new ArrayList<>())
                        .build();
            }
            if (charts.getDiagnosisSessions() == null) {
                charts = ChartStatsResponse.builder()
                        .userRegistrations(charts.getUserRegistrations() != null ? charts.getUserRegistrations() : new ArrayList<>())
                        .diagnosisSessions(new ArrayList<>())
                        .build();
            }
            LocalDateTime startLogs = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endLogs = endDate != null ? endDate.atTime(java.time.LocalTime.MAX) : null;

            Page<SystemLogResponse> recentLogs = systemLogService.getLogs( null, null, startLogs, endLogs,
                    PageRequest.of(0, 10, Sort.by("performedAt").descending())
            );
            List<SystemLogResponse> logList = recentLogs != null ? recentLogs.getContent() : new ArrayList<>();
            model.addAttribute("stats", stats);
            model.addAttribute("charts", charts);
            model.addAttribute("recentLogs", logList);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            return "admin/dashboard";

        } catch (Exception e) {
            log.error("ERROR loading dashboard: ", e);

            model.addAttribute("stats", DashboardStatsResponse.builder()
                    .totalUsers(0L)
                    .totalDoctors(0L)
                    .totalPatients(0L)
                    .blockedUsers(0L)
                    .totalDiagnosisSessions(0L)
                    .build());

            model.addAttribute("charts", ChartStatsResponse.builder()
                    .userRegistrations(new ArrayList<>())
                    .diagnosisSessions(new ArrayList<>())
                    .build());

            model.addAttribute("recentLogs", new ArrayList<>());
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("error", "Không thể tải dữ liệu dashboard. Vui lòng thử lại.");

            return "admin/dashboard";
        }
    }

    @GetMapping("/users")
    public String userManagement(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<UserResponse> users = adminService.getUser(keyword, role, status, pageable);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("roles", new String[]{"", "ADMIN", "DOCTOR", "PATIENT"});
        model.addAttribute("statuses", new String[]{"", "ACTIVE", "INACTIVE", "BANNED"});
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Integer id, Model model) {
        UserDetailResponse userDetail = adminService.getUserDetail(id);
        model.addAttribute("user", userDetail);
        return "admin/user-detail";
    }

    @PostMapping("/users/{id}/status")
    public String updateUserStatus(
            @PathVariable Integer id,
            @Valid @ModelAttribute UpdateUserStatusRequest request,
            BindingResult bindingResult,
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
            boolean isUpdated = adminService.updateUserStatus(request);
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

    @GetMapping("/create-doctor")
    public String createDoctorPage(@RequestParam(defaultValue = "1") int step,
                                   @RequestParam(required = false) Integer remainingTime,
                                   Model model) {
        if (!model.containsAttribute("doctorRequest")) {
            model.addAttribute("doctorRequest", new InitiateCreateDoctorRequest());
        }
        String adminEmail = "luugiang205@gmail.com";

        int remainingTimeValue;
        if (remainingTime != null) {
            remainingTimeValue = remainingTime;
        } else {
            remainingTimeValue = OtpUtil.getRemainingTime(adminEmail);
        }
        model.addAttribute("step", step);
        model.addAttribute("remainingTime", remainingTimeValue);
        return "admin/create-doctor";
    }

    @PostMapping("/create-doctor/initiate")
    public String initiateCreateDoctor(
            @Valid @ModelAttribute("doctorRequest") InitiateCreateDoctorRequest request,
            BindingResult result,
            @RequestParam(required = false) MultipartFile certificateFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("doctorRequest", request);
            redirectAttributes.addFlashAttribute("hasErrors", true);
            redirectAttributes.addFlashAttribute("errors", result.getAllErrors());
            return "redirect:/admin/create-doctor";
        }
        try {
            if (certificateFile != null && !certificateFile.isEmpty()) {
                request.setCertificateFile(certificateFile);
            }
            User adminUser = (userDetails != null) ? userDetails.getUser() :
                    adminService.getAdminUser();
            InitiateCreateDoctorResponse response = adminService.initiateCreateDoctor(request, adminUser);
            redirectAttributes.addFlashAttribute("requestId", response.getRequestId());
            redirectAttributes.addFlashAttribute("step", 2);
            return "redirect:/admin/create-doctor/verify";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("doctorRequest", request);
            return "redirect:/admin/create-doctor";
        }
    }

    @GetMapping("/create-doctor/verify")
    public String verifyDoctorPage(Model model) {
        if (!model.containsAttribute("step")) {
            model.addAttribute("step", 2);
        }
        if (!model.containsAttribute("remainingTime")) {
            String adminEmail = "luugiang205@gmail.com";
            int remainingTime = OtpUtil.getRemainingTime(adminEmail);
            model.addAttribute("remainingTime", remainingTime);
        }
        return "admin/create-doctor";
    }

    @PostMapping("/create-doctor/confirm")
    public String confirmCreateDoctor(
            @Valid @ModelAttribute VerifyPendingDoctorRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User adminUser = (userDetails != null) ? userDetails.getUser() : adminService.getAdminUser();
        adminService.verifyAndCreateDoctor(request, adminUser);

        redirectAttributes.addFlashAttribute("success", "Tạo tài khoản bác sĩ thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/create-doctor/resend-otp")
    @ResponseBody
    public Map<String, Object> resendOtp() {
        String adminEmail = "luugiang205@gmail.com";
        return adminService.resendOtp(adminEmail);
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
}