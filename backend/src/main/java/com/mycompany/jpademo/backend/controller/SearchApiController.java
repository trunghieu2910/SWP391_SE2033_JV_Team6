package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.GlobalSearchResponse;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: GiangLTHE194888
 * Task: Provides API endpoints for admin global search operations.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SearchApiController {

    private final AdminService adminService;

    /** Performs a global keyword search for admin dashboard data. */
    @GetMapping("/search")
    public ResponseEntity<GlobalSearchResponse> searchGlobal(
            @RequestParam(required = false) String keyword) {
        GlobalSearchResponse result = adminService.searchGlobal(keyword);
        return ResponseEntity.ok(result);
    }
}