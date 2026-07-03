package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.service.impl.LisMockDataProvider;
import com.mycompany.jpademo.backend.service.interfaces.LisIntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Gồm 2 luồng dùng chung 1 nghiệp vụ (receiveLabResults) nhưng khác cơ chế xác thực:
 *   1) /results   — Webhook mô phỏng LIS/HIS THẬT gọi vào, xác thực bằng X-API-Key, KHÔNG cần đăng nhập.
 *   2) /simulate  — Hành động nội bộ do bác sĩ bấm nút trên UI Thymeleaf, xác thực bằng Session (formLogin).
 */
@RestController
@RequestMapping("/api/integration/lis")
@RequiredArgsConstructor
public class LisIntegrationController {

    private final LisIntegrationService lisIntegrationService;
    private final LisMockDataProvider lisMockDataProvider;

    @Value("${lis.integration.api-key}")
    private String configuredApiKey;

    // ===================================================================
    // 1) WEBHOOK GỐC — giữ nguyên 100% logic như trước
    // ===================================================================
    @PostMapping("/results")
    @PreAuthorize("permitAll()") // tách khỏi hasRole('DOCTOR') vì caller là hệ thống ngoài, không có session
    public ResponseEntity<ApiResponse<LabResultResponse>> receiveResults(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody LisResultRequest request) {

        if (apiKey == null || !apiKey.equals(configuredApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.<LabResultResponse>builder()
                            .code(401)
                            .success(false)
                            .message("API key không hợp lệ hoặc bị thiếu")
                            .build());
        }

        LabResultResponse data = lisIntegrationService.receiveLabResults(request);

        return ResponseEntity.ok(
                ApiResponse.<LabResultResponse>builder()
                        .code(200)
                        .success(true)
                        .message("Đã nhận và cập nhật kết quả xét nghiệm từ LIS thành công")
                        .data(data)
                        .build());
    }

    // ===================================================================
    // 2) HÀNH ĐỘNG NỘI BỘ — nút "Lấy kết quả LIS" trên giao diện Thymeleaf
    //    Gọi THẲNG service, không dựng lại request HTTP, không dùng X-API-Key.
    // ===================================================================
    @PostMapping("/simulate/{sessionId}/{labResultId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void simulateFromUi(@PathVariable Integer sessionId,
                               @PathVariable Integer labResultId,
                               @RequestParam String testType,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) throws IOException {

        List<LisResultRequest.TestResultItem> mockResults = lisMockDataProvider.getMockResults(testType);

        if (mockResults.isEmpty()) {
            httpRequest.getSession().setAttribute("flashError",
                    "Chưa có dữ liệu mẫu LIS cho loại xét nghiệm: \"" + testType + "\"");
            httpResponse.sendRedirect("/doctor/sessions/" + sessionId);
            return;
        }

        LisResultRequest lisRequest = new LisResultRequest();
        lisRequest.setLabResultId(labResultId);
        lisRequest.setTestResults(mockResults);

        try {
            lisIntegrationService.receiveLabResults(lisRequest); // TÁI SỬ DỤNG y nguyên service cũ
            httpRequest.getSession().setAttribute("flashSuccess", "Đã nhận kết quả xét nghiệm thành công!");
        } catch (ResourceNotFoundException e) {
            httpRequest.getSession().setAttribute("flashError", e.getMessage());
        }

        httpResponse.sendRedirect("/doctor/sessions/" + sessionId);
    }
}