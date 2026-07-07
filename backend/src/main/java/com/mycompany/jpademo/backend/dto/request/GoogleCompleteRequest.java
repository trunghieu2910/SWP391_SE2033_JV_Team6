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
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.")
    private String phoneNumber;

    @NotBlank
    @Size(min = 12, max = 12, message = "CCCD phải đúng 12 ký tự")
    private String nationalID;
}
