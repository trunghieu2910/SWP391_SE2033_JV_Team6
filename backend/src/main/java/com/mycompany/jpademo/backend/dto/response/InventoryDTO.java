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
    private Long daysUntilExpiry;
    private Boolean isExpiringSoon;
    private Boolean isLowStock;

    // Display fields for Warehouse Inventory
    private String drugCode;
    private java.time.LocalDate expiryDate;
    private Integer quantityInLargeUnit;
    private String largeUnitName;
    private String smallUnitName;
}
