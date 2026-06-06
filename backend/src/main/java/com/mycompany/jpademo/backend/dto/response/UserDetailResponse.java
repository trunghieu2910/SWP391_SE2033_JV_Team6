package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserDetailResponse {
    private UserResponse userResponse;

    private List<SystemLogResponse> systemLogResponses;
}
