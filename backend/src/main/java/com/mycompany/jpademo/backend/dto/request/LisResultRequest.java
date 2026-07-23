package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DTO representing the result payload sent by an external LIS/HIS
 * system (or simulated internally) when lab results for a given
 * labResultId become available.
 */
@Data
public class LisResultRequest {

    // Identifies which PENDING LabResult these incoming values belong to.
    @NotNull(message = "labResultId không được để trống")
    private Integer labResultId;

    // One entry per measured parameter reported for this lab order.
    @NotEmpty(message = "Danh sách kết quả xét nghiệm không được để trống")
    @Valid
    private List<TestResultItem> testResults;

    /**
     * A single measured parameter within a lab result
     * (e.g. "WBC" = "1.5", unit = "G/L").
     */
    @Data
    public static class TestResultItem {

        @NotBlank(message = "testName không được để trống")
        @Size(max = 100, message = "testName không được vượt quá 100 ký tự")
        private String testName;

        @NotBlank(message = "resultValue không được để trống")
        @Size(max = 100, message = "resultValue không được vượt quá 100 ký tự")
        private String resultValue;

        // Only used when this Parameter needs to be newly created
        // (if the Parameter already exists in the catalog, its
        // existing unit is kept as-is and this value is ignored).
        @Size(max = 50, message = "unit không được vượt quá 50 ký tự")
        private String unit;
    }
}

