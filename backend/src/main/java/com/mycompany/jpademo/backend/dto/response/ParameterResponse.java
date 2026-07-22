package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * A single parameter value belonging to a lab result, flattened
 * from the underlying LabResultParameter + Parameter entities
 * for easier rendering in Thymeleaf templates.
 */
@Getter
@Builder
public class ParameterResponse {

    private Integer parameterId;
    private String parameterName;
    private String unit;
}
