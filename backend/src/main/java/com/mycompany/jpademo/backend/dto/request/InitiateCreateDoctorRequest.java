package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiateCreateDoctorRequest {
    @NotBlank(message = "Username is required!")
    private String userName;

    @NotBlank(message = "Fullname is required!")
    private String fullName;

    @Email(message = "Email should be valid!")
    @NotBlank(message = "Email is required!")
    private String email;

    @NotBlank(message = "Phone number is required!")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Invalid phone number")
    private String phoneNumber;

    @Size(min = 9, max = 12, message = "National ID is required")
    private String nationalId;

    private MultipartFile certificateFile;
}