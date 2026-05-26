package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/medical-records")

public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private DiagnosisSessionRepository sessionRepository;

    /**
     * API 1: Lấy danh sách lịch sử tất cả các ca khám cũ của một bệnh nhân
     * URL test Postman: GET http://localhost:8080/api/medical-records/patient/{patientID}
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {

        com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse detail =
                medicalRecordService.getMedicalRecordDetail(id);

        return ResponseEntity.ok(detail);
    }
    @GetMapping
    public List<Map<String, Object>> getAllRecords() {

        return sessionRepository.findAllMedicalRecords();
    }
    @GetMapping("/patient/{patientId}")
    public List<Map<String, Object>> getRecordsByPatient(@PathVariable Integer patientId) {
        // Trả về danh sách bệnh án riêng của bệnh nhân đó
        return sessionRepository.findMedicalRecordsByPatientId(patientId);
    }

}
