package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugBatchDTO {
    private Integer batchId;
    private Integer drugId;
    private String drugName;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private String unitName;
    private Integer unitId;
    private Integer quantity;
    private BigDecimal importPrice;
    private String supplier;
    private LocalDateTime importDate;
    private String importedBy;
    private Byte status;
    private String notes;
    private Integer quantityInStock;
    private Long daysUntilExpiry;
}
