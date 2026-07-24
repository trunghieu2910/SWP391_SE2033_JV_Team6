package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugDTO {
    private Integer drugId;
    private String drugCode;
    private String drugName;
    private String strength;
    private String strengthUnit;
    private String dosageForm;
    private String routeOfAdministration;
    private Integer subCategoryId;
    private String subCategoryName;
    private Integer baseUnitId;
    private String baseUnitName;
    private String manufacturer;
    private String countryOfOrigin;
    private String storageCondition;
    private String notes;
    private com.mycompany.jpademo.backend.enums.DrugStatus status;
    private Integer totalQuantityInStock;
    private Integer totalBatches;
    private Integer createdBy;
    private String createdByName;
    private String createdAt;
    private Integer updatedBy;
    private String updatedByName;
    private String updatedAt;
    private String updateReason;
}
