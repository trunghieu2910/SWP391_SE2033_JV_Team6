package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private Boolean success;

    private Integer code;

    private String message;

    private T data;
}
