package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/medical-records")
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
    public List<MedicalRecordResponse> getRecordsByPatient(@PathVariable Integer patientId) {
        return medicalRecordService.getPatientMedicalRecords(patientId);
    }

    // 3. API BẬT / TẮT CÔNG BỐ BỆNH ÁN (CHỈ BÁC SĨ MỚI CÓ QUYỀN GỌI)
    @PutMapping("/{sessionId}/visibility")
    public ResponseEntity<ApiResponse> toggleVisibility(
            @PathVariable Integer sessionId,
            @RequestParam boolean isShared,
            @RequestParam Integer doctorId
    ) {

        medicalRecordService.toggleRecordVisibility(sessionId, doctorId, isShared);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật quyền công bố bệnh án thành công!")
                .build());
    }

    // 4. API LẤY CHI TIẾT 1 BỆNH ÁN (ĐÃ TÍCH HỢP CHE DỮ LIỆU)
    @GetMapping("/detail/{id}")
    public ResponseEntity<MedicalRecordDetailResponse> getDetail(
            @PathVariable Integer id,
            Authentication authentication) {

        // Kiểm tra xem Thẻ VIP của người gọi có chứa chức danh ROLE_PATIENT không
        boolean isPatient = false;
        if (authentication != null && authentication.getAuthorities() != null) {
            isPatient = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
        }

        // Truyền cờ isPatient xuống Service để nó biết đường che kết quả chẩn đoán
        MedicalRecordDetailResponse detail = medicalRecordService.getMedicalRecordDetail(id, isPatient);

        return ResponseEntity.ok(detail);
    }
}