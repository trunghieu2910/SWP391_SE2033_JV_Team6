package com.mycompany.jpademo.backend.cache;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingDoctorData {
    private String requestId;
    private String adminEmail;
    private String userName;
    private String fullName;
    private String email;
    private String nationalId;
    private String phoneNumber;
    private String certificateUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
}