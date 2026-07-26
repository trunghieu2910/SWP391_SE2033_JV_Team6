package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.PatientSearchResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import com.mycompany.jpademo.backend.service.interfaces.PatientSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/diagnosis-sessions")
@RequiredArgsConstructor
public class DiagnosisSessionController {
    private final DiagnosisSessionService diagnosisSessionService;
    private final PatientSearchService patientSearchService;

    /**
     * Tìm kiếm bệnh nhân theo từ khóa (tên hoặc nationalID)
     */
    @GetMapping("/search-patients")
        @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<PatientSearchResponse>>> searchPatients(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<PatientSearchResponse> patients = patientSearchService.searchPatients(keyword);
        return ResponseEntity.ok(
                ApiResponse.<List<PatientSearchResponse>>builder()
                        .code(200)
                        .message("Tìm kiếm bệnh nhân thành công")
                        .data(patients)
                        .build()
        );
    }



    /**
     * Bệnh nhân hoặc bác sĩ submit biểu mẫu triệu chứng
     */
    @PostMapping("/{sessionId}/symptom-result")
        @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    public ResponseEntity<ApiResponse<SymptomResultResponse>> submitSymptomForm(
            @PathVariable Integer sessionId,
            @Valid @RequestBody SubmitSymptomFormRequest request,
            Authentication authentication) {
        Integer userId = extractUserId(authentication);
        String userRole = extractUserRole(authentication);

        SymptomResultResponse response = diagnosisSessionService.submitSymptomForm(sessionId, request, userId, userRole);
        return ResponseEntity.ok(
                ApiResponse.<SymptomResultResponse>builder()
                        .code(200)
                        .message("Submit biểu mẫu triệu chứng thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Bác sĩ chỉnh sửa biểu mẫu triệu chứng
     */
    @PutMapping("/{sessionId}/symptom-result")
        @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<SymptomResultResponse>> updateSymptomForm(
            @PathVariable Integer sessionId,
            @Valid @RequestBody SubmitSymptomFormRequest request,
            Authentication authentication) {
        Integer userId = extractUserId(authentication);
        String userRole = extractUserRole(authentication);

        SymptomResultResponse response = diagnosisSessionService.submitSymptomForm(sessionId, request, userId, userRole);
        return ResponseEntity.ok(
                ApiResponse.<SymptomResultResponse>builder()
                        .code(200)
                        .message("Cập nhật biểu mẫu triệu chứng thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Lấy chi tiết ca khám
     */
    @GetMapping("/{sessionId}")
        @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    public ResponseEntity<ApiResponse<DiagnosisSessionResponse>> getSessionDetail(
            @PathVariable Integer sessionId) {
        DiagnosisSessionResponse response = diagnosisSessionService.getSessionDetail(sessionId);
        return ResponseEntity.ok(
                ApiResponse.<DiagnosisSessionResponse>builder()
                        .code(200)
                        .message("Lấy chi tiết ca khám thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Lấy chi tiết SymptomResult
     */
    @GetMapping("/{sessionId}/symptom-result")
        @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    public ResponseEntity<ApiResponse<SymptomResultResponse>> getSymptomResult(
            @PathVariable Integer sessionId) {
        SymptomResultResponse response = diagnosisSessionService.getSymptomResult(sessionId);
        return ResponseEntity.ok(
                ApiResponse.<SymptomResultResponse>builder()
                        .code(200)
                        .message("Lấy chi tiết kết quả triệu chứng thành công")
                        .data(response)
                        .build()
        );
    }

    private Integer extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser().getUserId();
        }
        throw new RuntimeException("Không thể lấy userId từ authentication");
    }

    private String extractUserRole(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("UNKNOWN");
        }
        return "UNKNOWN";
    }
}
