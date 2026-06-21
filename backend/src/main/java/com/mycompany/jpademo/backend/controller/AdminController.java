package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.dto.response.DashboardStatsResponse;
import com.mycompany.jpademo.backend.dto.response.InitiateCreateDoctorResponse;
import com.mycompany.jpademo.backend.dto.response.UserDetailResponse;
import com.mycompany.jpademo.backend.dto.response.UserResponse;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUser(
            @RequestParam(required = false)
            String keyword,
            @RequestParam(required = false)
            String role,
            @RequestParam(required = false)
            UserStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getUser(keyword, role, status, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getUserDetail(id));
    }

    @PatchMapping("/users/status")
    public ResponseEntity<String> updateUserStatus(
            @Valid
            @RequestBody UpdateUserStatusRequest request) {
        return adminService.updateUserStatus(request);
    }

    @PostMapping("/doctors/initiate")
    public ResponseEntity<InitiateCreateDoctorResponse> initiateCreateDoctor(
            @Valid
            @RequestBody InitiateCreateDoctorRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(adminService.initiateCreateDoctor(request, userDetails.getUser()));
    }

    @PostMapping("/doctors/confirm")
    public ResponseEntity<String> confirmCreateDoctor(
            @Valid
            @RequestBody VerifyPendingDoctorRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return adminService.verifyAndCreateDoctor(request, userDetails.getUser());
    }
}

