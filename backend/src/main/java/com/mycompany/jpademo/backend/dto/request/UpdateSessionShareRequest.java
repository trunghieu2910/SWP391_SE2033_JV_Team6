package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionShareRequest {
    @NotNull(message = "Session ID must not be null")
    private Integer sessionId;

    @NotNull(message = "isShare cannot be null")
    private Boolean isShared;
}
