package com.mycompany.jpademo.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginResponse {
    private String status;
    private String email;
    private String fullName;
}
