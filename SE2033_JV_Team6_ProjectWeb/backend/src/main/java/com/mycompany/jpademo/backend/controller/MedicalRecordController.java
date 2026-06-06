package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @GetMapping
    public ResponseEntity<Page<MedicalRecordResponse>> getMedicalRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isShared,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(medicalRecordService.getMedicalRecords(keyword, status, isShared, pageable));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<MedicalRecordDetailResponse> getMedicalRecordDetail(
            @PathVariable Integer sessionId) {

        MedicalRecordDetailResponse detail = medicalRecordService.getMedicalRecordDetail(sessionId, false);
        return ResponseEntity.ok(detail);
    }
}