package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO {
    private Integer inventoryId;
    private Integer batchId;
    private String batchNumber;
    private Integer drugId;
    private String drugName;
    private Integer quantityInStock;
    private LocalDateTime lastUpdated;
    private Byte status;
    private Long daysUntilExpiry;
    private Boolean isExpiringSoon;
    private Boolean isLowStock;
}
