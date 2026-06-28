package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSearchDTO {
    private Integer userId;
    private String userName;
    private String fullName;
    private String email;
    private String roleName;
    private String status;
    private String avatarUrl;
}