package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Past;
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

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^(|0[0-9]{9,10})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @Pattern(regexp = "^(|[0-9]{9,12})$", message = "CCCD/CMND chỉ được gồm số và dài 9-12 ký tự")
    private String nationalID;

    @Pattern(regexp = "^(|Male|Female|Other)$", message = "Giới tính không hợp lệ")
    private String gender;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dob;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;
}