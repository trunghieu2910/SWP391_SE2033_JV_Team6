package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugDTO {
    private Integer drugId;
    private String drugCode;
    private String drugName;
    private BigDecimal strength;
    private String strengthUnit;
    private String dosageForm;
    private String routeOfAdministration;
    private String subCategoryName;
    private Integer subCategoryId;
    private String packaging;
    private String manufacturer;
    private String countryOfOrigin;
    private String storageCondition;
    private Integer shelfLifeMonths;
    private String notes;
    private Byte status;
    private Integer totalQuantityInStock;
    private Integer totalBatches;
}
