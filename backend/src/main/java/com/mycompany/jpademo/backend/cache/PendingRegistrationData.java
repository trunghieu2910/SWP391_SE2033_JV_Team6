package com.mycompany.jpademo.backend.cache;

import com.mycompany.jpademo.backend.dto.request.RegisterRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistrationData {
    private RegisterRequest request;
    private LocalDateTime expireTime;
}
