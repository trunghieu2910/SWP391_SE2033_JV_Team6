package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.LabResultResponse;
import com.mycompany.jpademo.backend.service.interfaces.LisIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint webhook mô phỏng LIS/HIS gửi kết quả xét nghiệm.
 * Xác thực dựa trên API Key tĩnh trong header "X-API-Key".
 */
@RestController
@RequestMapping("/api/integration/lis")
@RequiredArgsConstructor
public class LisIntegrationController {

    private final LisIntegrationService lisIntegrationService;

    @Value("${lis.integration.api-key}")
    private String configuredApiKey;

    @PostMapping("/results")
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
}

