package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParameterResponse {

    private Integer parameterId;
    private String parameterName;
    private String unit;
}
