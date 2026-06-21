package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.validation.annotation.Validated;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/medical-records")
@Validated
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    // 1. LẤY DANH SÁCH TẤT CẢ (CHO BÁC SĨ)
    @GetMapping
    public List<MedicalRecordResponse> getAllRecords() {
        return medicalRecordService.getAllMedicalRecords();
    }

    // 2. LẤY DANH SÁCH RIÊNG (CHO BỆNH NHÂN)
    @GetMapping("/patient/{patientId}")
    public List<MedicalRecordResponse> getRecordsByPatient(@PathVariable @jakarta.validation.constraints.Min(value = 1, message = "Validation Error") Integer patientId) {
        return medicalRecordService.getPatientMedicalRecords(patientId);
    }

    // 3. LẤY DANH SÁCH VỚI FILTER VÀ PAGINATION (từ folder 'sua')
    @GetMapping("/filter")
    public ResponseEntity<Page<MedicalRecordResponse>> getMedicalRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isShared,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(medicalRecordService.getMedicalRecords(keyword, status, isShared, pageable));
    }

    // 4. API LẤY CHI TIẾT 1 BỆNH ÁN (ĐÃ TÍCH HỢP CHE DỮ LIỆU)
    @GetMapping("/detail/{id}")
    public ResponseEntity<MedicalRecordDetailResponse> getDetail(
            @PathVariable @jakarta.validation.constraints.Min(value = 1, message = "Validation Error") Integer id,
            Authentication authentication) {

        // Kiểm tra xem người gọi có ROLE_PATIENT không
        boolean isPatient = false;
        if (authentication != null && authentication.getAuthorities() != null) {
            isPatient = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        }

        // Truyền cờ isPatient xuống Service để che kết quả chẩn đoán nếu cần
        MedicalRecordDetailResponse detail = medicalRecordService.getMedicalRecordDetail(id, isPatient);

        return ResponseEntity.ok(detail);
    }

    // 5. API LẤY CHI TIẾT THEO PATH /{sessionId} (tương thích với folder 'sua')
    @GetMapping("/{sessionId}")
    public ResponseEntity<MedicalRecordDetailResponse> getMedicalRecordDetail(
            @PathVariable @jakarta.validation.constraints.Min(value = 1, message = "Validation Error") Integer sessionId) {

        MedicalRecordDetailResponse detail = medicalRecordService.getMedicalRecordDetail(sessionId, false);
        return ResponseEntity.ok(detail);
    }

    // 6. API CẬP NHẬT TRẠNG THÁI SHARE BỆNH ÁN
    @PutMapping("/{sessionId}/visibility")
    public ResponseEntity<ApiResponse> toggleVisibility(
            @PathVariable @jakarta.validation.constraints.Min(value = 1, message = "Validation Error") Integer sessionId,
            @RequestParam Boolean isShared) {
        
        medicalRecordService.updateMedicalRecordVisibility(sessionId, isShared);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật quyền công bố bệnh án thành công!")
                .build());
    }
}