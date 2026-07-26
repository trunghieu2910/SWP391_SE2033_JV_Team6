package com.mycompany.jpademo.backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispenseDrugRequest {
    private Integer detailId;
    private Integer batchId;
    private Integer quantityDispensed;
    private String notes;
    private com.mycompany.jpademo.backend.enums.PrescriptionDetailStatus actionStatus;
}
