package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionStatusRequest {
    @NotNull(message = "Session ID must not be null")
    private Integer sessionId;

    @NotBlank(message = "Status must not be blank")
    private String status;
}
