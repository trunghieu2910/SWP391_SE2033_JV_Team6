package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// [Nguyen The Hieu]: DTO này chứa dữ liệu trả về cho màn hình 1 (tổng quan tải bác sĩ).
// Gom nhóm số lượng ca theo từng trạng thái (PENDING, PROCESSING, FAILED) và tổng số ca đang hoạt động (totalActive) của một bác sĩ.
public class DoctorWorkloadResponse {
    private Integer doctorId;
    private String doctorName;
    private long pendingCount;
    private long processingCount;
    private long failedCount;
    private long totalActive;
}
