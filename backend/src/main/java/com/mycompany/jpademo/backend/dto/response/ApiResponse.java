package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private Integer code;

    private Boolean success;

    private String message;

    private T data;
}
