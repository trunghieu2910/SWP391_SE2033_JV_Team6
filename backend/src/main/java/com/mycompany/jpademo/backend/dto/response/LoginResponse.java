package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private Integer userId;

    private String username;

    private String email;

    private String role;
}

