package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * API 1: Lấy danh sách lịch sử tất cả các ca khám cũ của một bệnh nhân
     * URL test Postman: GET http://localhost:8080/api/medical-records/patient/{patientID}
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) { // Sửa thành Integer cho khớp mã ID ca khám của ông
        // Gọi qua tầng Service Impl để nó tự động gom Patient, LabResult, Review thành cục DTO sạch rác
        com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse detail =
                medicalRecordService.getMedicalRecordDetail(id);

        return ResponseEntity.ok(detail);
    }


}
