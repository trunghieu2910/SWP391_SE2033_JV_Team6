package com.mycompany.jpademo.backend.dto.request;

import com.mycompany.jpademo.backend.aop.interfaces.LoggableTarget;
import com.mycompany.jpademo.backend.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest  implements LoggableTarget {
    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @NotNull(message = "Status cannot be null")
    private UserStatus status;

    @NotBlank(message = "Reason cannot be null")
    @Size(min = 5, max = 255, message = "Reason must be between 5 and 255 characters")
    private String reason;

    @Override
    public Integer getTargetId() {
        return userId;
    }
}
