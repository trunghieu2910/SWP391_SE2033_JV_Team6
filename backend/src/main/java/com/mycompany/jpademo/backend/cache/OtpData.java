package com.mycompany.jpademo.backend.cache;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpData {

    private String otp;
    private LocalDateTime expireTime;

    @Builder.Default
    private int failedAttempts = 0;  
}