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
    @NotNull(message = "User ID không thể bỏ trống")
    private Integer userId;

    @NotNull(message = "Trạng thái không thể bỏ trống")
    private UserStatus status;

    @NotBlank(message = "Reason cannot be null")
    @Size(min = 5, max = 255, message = "Lý do phải từ 5 đến 255 ký tự")
    private String reason;

    @Override
    public Integer getTargetId() {
        return userId;
    }
}
