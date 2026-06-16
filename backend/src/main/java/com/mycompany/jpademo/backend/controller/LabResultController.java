package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreateLabResultRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.service.interfaces.LabResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/lab-results")
@RequiredArgsConstructor
public class LabResultController {

    private final LabResultService labResultService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<LabResultResponse>> createLabResult(
            @Valid @RequestBody CreateLabResultRequest request) {

        LabResultResponse created = labResultService.createLabResult(request);

        return ResponseEntity
                .status(HttpStatus.CREATED) // Trả về 201 thay vì 200 vì đây là hành động tạo mới
                .body(ApiResponse.<LabResultResponse>builder()
                        .code(201)
                        .success(true)
                        .message("Tạo chỉ định xét nghiệm thành công")
                        .data(created)
                        .build());
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')") // Cả 2 role đều xem được
    public ResponseEntity<ApiResponse<List<LabResultResponse>>> getLabResultsBySession(
            @PathVariable Integer sessionId) {

        List<LabResultResponse> results = labResultService.getLabResultsBySession(sessionId);

        return ResponseEntity.ok(
                ApiResponse.<List<LabResultResponse>>builder()
                        .code(200)
                        .success(true)
                        .message("Lấy danh sách xét nghiệm thành công")
                        .data(results)
                        .build());
    }
}
