package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * DTO mô phỏng gói tin mà LIS/HIS gửi sang hệ thống AI khi có kết quả
 * xét nghiệm mới.
 */
@Data
public class LisResultRequest {

    @NotNull(message = "labResultId không được để trống")
    private Integer labResultId;

    @NotEmpty(message = "Danh sách kết quả xét nghiệm không được để trống")
    @Valid
    private List<TestResultItem> testResults;

    @Data
    public static class TestResultItem {

        @NotBlank(message = "testName không được để trống")
        private String testName;

        @NotBlank(message = "resultValue không được để trống")
        private String resultValue;

        // Chỉ dùng khi cần TẠO MỚI Parameter (nếu Parameter đã tồn tại thì giữ unit cũ)
        private String unit;
    }
}

