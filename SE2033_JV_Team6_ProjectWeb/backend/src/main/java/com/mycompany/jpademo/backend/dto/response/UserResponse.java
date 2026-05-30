package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Integer userId;

    private String userName;

    private String fullName;

    private String email;

    private UserStatus status;

    private RoleName roleName;

    private LocalDateTime lastChangePassTime;

    private LocalDateTime createdAt;
}
