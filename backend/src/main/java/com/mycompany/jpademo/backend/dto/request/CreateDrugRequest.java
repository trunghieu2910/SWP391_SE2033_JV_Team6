package com.mycompany.jpademo.backend.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDrugRequest {
    private String drugCode;
    private String drugName;
    private String strength;
    private String strengthUnit;
    private String dosageForm;
    private String routeOfAdministration;
    private Integer subCategoryId;
    private Integer baseUnitId;
    private String manufacturer;
    private String countryOfOrigin;
    private String storageCondition;
    private List<Integer> conversionLargeUnitIds;
    private List<Integer> conversionSmallUnitIds;
    private List<Integer> conversionQuantities;
    private String notes;
}



