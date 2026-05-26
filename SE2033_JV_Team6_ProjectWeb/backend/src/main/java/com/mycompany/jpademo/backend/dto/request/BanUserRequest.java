package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BanUserRequest {
    @NotNull(message = "User ID cannot be null")
    private int userId;

    @NotNull(message = "Reason cannot be null")
    private String reason;
}
