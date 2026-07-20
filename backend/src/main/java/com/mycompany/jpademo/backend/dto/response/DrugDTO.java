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
    private String subCategoryName;
    private Integer subCategoryId;
    private Integer baseUnitId;
    private String baseUnitName;
    private String packaging;
    private String manufacturer;
    private String countryOfOrigin;
    private String storageCondition;
    private Integer shelfLifeMonths;
    private String notes;
    private Byte status;
    private Integer totalQuantityInStock;
    private Integer totalBatches;
    private String createdByName;
    private String createdAt;
}
