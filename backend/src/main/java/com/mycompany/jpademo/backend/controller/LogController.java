package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.SystemLogResponse;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class LogController {
    private final SystemLogService systemLogService;

    @GetMapping("/logs")
    public ResponseEntity<Page<SystemLogResponse>> getLogs(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword,
            @PageableDefault(page = 0, size = 10, sort = "performedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(systemLogService.getLogs(userId, action, keyword, pageable));
    }
}
