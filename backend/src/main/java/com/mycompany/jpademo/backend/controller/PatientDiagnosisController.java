package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreatePatientSessionRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.enums.ClinicalInputMode;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.enums.MedicalImageStatus;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/patient/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientDiagnosisController {
    private final DiagnosisSessionService diagnosisSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<DiagnosisSessionResponse>> createSessionForPatient(
            @RequestBody(required = false) CreatePatientSessionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        var user = userDetails.getUser();
        DiagnosisSessionResponse response = diagnosisSessionService.createSessionForPatient(request, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DiagnosisSessionResponse>builder()
                        .code(201)
                        .success(true)
                        .message("Tạo phiên khám thành công")
                        .data(response)
                        .build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<DiagnosisSessionResponse>> getActiveSession(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        DiagnosisSessionResponse resp = diagnosisSessionService.getActiveSessionForPatient(user);

        return ResponseEntity.ok(ApiResponse.<DiagnosisSessionResponse>builder()
                .code(200)
                .success(true)
                .message("Lấy phiên khám đang hoạt động")
                .data(resp)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<DiagnosisSessionResponse>>> getSessions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        var list = diagnosisSessionService.getSessionsForPatient(user);

        return ResponseEntity.ok(ApiResponse.<java.util.List<DiagnosisSessionResponse>>builder()
                .code(200)
                .success(true)
                .message("Lấy danh sách phiên khám")
                .data(list)
                .build());
    }
}
