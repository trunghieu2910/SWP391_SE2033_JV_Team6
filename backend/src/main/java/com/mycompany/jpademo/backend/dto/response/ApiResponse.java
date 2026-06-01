package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private Boolean success;

    private String message;

    private Object data;
}
