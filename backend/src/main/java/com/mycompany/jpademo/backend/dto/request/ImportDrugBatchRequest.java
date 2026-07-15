package com.mycompany.jpademo.backend.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportDrugBatchRequest {
    private Integer drugId;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private Integer unitId;
    private Integer quantity;
    private BigDecimal importPrice;
    private String supplier;
    private String notes;
}
