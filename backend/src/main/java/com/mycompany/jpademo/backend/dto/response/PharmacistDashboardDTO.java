package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacistDashboardDTO {
    private Integer totalDrugs;
    private Integer pendingPrescriptions;
    private Integer expiringDrugs; // 7 days
    private Integer lowStockDrugs;
    private Integer totalBatches;
    private LocalDateTime lastUpdate;
}
