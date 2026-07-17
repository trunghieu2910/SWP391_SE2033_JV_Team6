package com.mycompany.jpademo.backend.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDrugRequest {
    private String drugCode;
    private String drugName;
    private BigDecimal strength;
    private String strengthUnit;
    private String dosageForm;
    private String routeOfAdministration;
    private Integer subCategoryId;
    private String packaging;
    private String manufacturer;
    private String countryOfOrigin;
    private String storageCondition;
    private Integer shelfLifeMonths;
    private String notes;
}
