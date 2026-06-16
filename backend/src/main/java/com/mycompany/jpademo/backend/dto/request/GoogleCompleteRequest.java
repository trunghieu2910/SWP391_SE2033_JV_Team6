package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCompleteRequest {
    @NotBlank
    private String idToken;

    @NotBlank
    @Size(min = 3, max = 50)
    private String userName;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải đúng 10 chữ số.")
    private String phoneNumber;

    @NotBlank
    @Size(min = 12, max = 12, message = "CCCD phải đúng 12 ký tự")
    private String nationalID;
}
