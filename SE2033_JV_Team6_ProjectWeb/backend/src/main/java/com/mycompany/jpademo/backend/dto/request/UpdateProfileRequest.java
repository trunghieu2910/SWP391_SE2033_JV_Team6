package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    private String username;

    @Size(max = 100, message = "Full name không được vượt quá 100 ký tự")
    private String fullName;

    private String phoneNumber;

    @Size(min = 12, max = 12, message = "National ID phải có đúng 12 ký tự")
    private String nationalID;

    private String gender;

    private LocalDate dob;

    private String address;
}