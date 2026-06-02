package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;


import java.time.LocalDateTime;

@Getter
@Builder
public class UserRespone {
    private Integer userId;

    private String username;

    private String fullName;

    private String email;

    private UserStatus status;

    private String roleName;

    private String phoneNumber;

    private LocalDateTime createdAt;
}
