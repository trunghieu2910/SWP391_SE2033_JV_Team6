package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.GlobalSearchResponse;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SearchApiController {

    private final AdminService adminService;

    @GetMapping("/search")
    public ResponseEntity<GlobalSearchResponse> searchGlobal(
            @RequestParam(required = false) String keyword) {
        GlobalSearchResponse result = adminService.searchGlobal(keyword);
        return ResponseEntity.ok(result);
    }
}