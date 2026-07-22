package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.impl.LisMockDataProvider;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import com.mycompany.jpademo.backend.service.interfaces.LisIntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Exposes two entry points into the same business logic
 * ({@code receiveLabResults}) with different authentication
 * mechanisms:
 *   1) /results  — webhook for a REAL external LIS/HIS system,
 *                  authenticated via an X-API-Key header, no login required.
 *   2) /simulate — internal action triggered by a doctor from the
 *                  Thymeleaf UI, authenticated via the normal session (form login).
 */
@RestController
@RequestMapping("/api/integration/lis")
@RequiredArgsConstructor
public class LisIntegrationController {

    private final LisIntegrationService lisIntegrationService;
    private final LisMockDataProvider lisMockDataProvider;
    private final DoctorDiagnosisService doctorDiagnosisService;

    @Value("${lis.integration.api-key}")
    private String configuredApiKey;

    // ===================================================================
    // 1) EXTERNAL WEBHOOK — untouched core logic, unauthenticated at the
    //    Spring Security layer; authorization is done manually via API key below.
    // ===================================================================
    /** Receives real lab results from an external LIS/HIS system, validated by a shared API key. */
    @PostMapping("/results")
    @PreAuthorize("permitAll()") // tách khỏi hasRole('DOCTOR') vì caller là hệ thống ngoài, không có session
    public ResponseEntity<ApiResponse<LabResultResponse>> receiveResults(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody LisResultRequest request) {

        if (apiKey == null || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                configuredApiKey.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.<LabResultResponse>builder()
                            .code(401)
                            .success(false)
                            .message("API key không hợp lệ hoặc bị thiếu")
                            .build());
        }

        LabResultResponse data = lisIntegrationService.receiveLabResults(request, "REAL_LIS");

        return ResponseEntity.ok(
                ApiResponse.<LabResultResponse>builder()
                        .code(200)
                        .success(true)
                        .message("Đã nhận và cập nhật kết quả xét nghiệm từ LIS thành công")
                        .data(data)
                        .build());
    }

    // ===================================================================
    // 2) INTERNAL ACTION — the "Lấy kết quả LIS" button on the Thymeleaf
    //    UI. Calls the service directly; no HTTP request is built, no X-API-Key used.
    // ===================================================================
    /**
     * Simulates receiving LIS results for the given lab order using
     * canned mock data, on behalf of the currently logged-in doctor.
     * Re-verifies session ownership before delegating to
     * {@link LisIntegrationService#receiveLabResults}, then redirects
     * back to the session detail page with a flash message either way.
     */
    @PostMapping("/simulate/{sessionId}/{labResultId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void simulateFromUi(@PathVariable Integer sessionId,
                               @PathVariable Integer labResultId,
                               @RequestParam String testType,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) throws IOException {

        Integer doctorId = userDetails.getUser().getUserId();

        try {
            doctorDiagnosisService.verifyDoctorOwnsSession(doctorId, sessionId);
        } catch (UnauthorizedActionException | ResourceNotFoundException e) {
            httpRequest.getSession().setAttribute("flashError", e.getMessage());
            httpResponse.sendRedirect("/doctor/sessions");
            return;
        }

        List<LisResultRequest.TestResultItem> mockResults = lisMockDataProvider.getMockResults(testType);

        if (mockResults.isEmpty()) {
            httpRequest.getSession().setAttribute("flashError",
                    "Chưa có dữ liệu mẫu LIS cho loại xét nghiệm: \"" + testType + "\"");
            httpResponse.sendRedirect("/doctor/sessions/" + sessionId + "?openLab=true");
            return;
        }

        LisResultRequest lisRequest = new LisResultRequest();
        lisRequest.setLabResultId(labResultId);
        lisRequest.setTestResults(mockResults);

        try {
            lisIntegrationService.receiveLabResults(lisRequest, "UI_SIMULATE"); // TÁI SỬ DỤNG y nguyên service cũ
            httpRequest.getSession().setAttribute("flashSuccess", "Đã nhận kết quả xét nghiệm thành công!");
        } catch (ResourceNotFoundException e) {
            httpRequest.getSession().setAttribute("flashError", e.getMessage());
        }

        httpResponse.sendRedirect("/doctor/sessions/" + sessionId + "?openLab=true");
    }
}