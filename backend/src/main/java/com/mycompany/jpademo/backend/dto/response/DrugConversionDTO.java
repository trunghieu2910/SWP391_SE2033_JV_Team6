package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugConversionDTO {
    private Integer largeUnitId;
    private String largeUnitName;
    private Integer smallUnitId;
    private String smallUnitName;
    private Integer conversionQuantity;
}
