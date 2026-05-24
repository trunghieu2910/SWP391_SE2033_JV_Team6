package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserRespone {
    private Long userID;

    private String username;

    private String fullName;

    private String email;

    private String status;

    private String roleName;

    private String urlImage;

    private LocalDateTime createdAt;
}
