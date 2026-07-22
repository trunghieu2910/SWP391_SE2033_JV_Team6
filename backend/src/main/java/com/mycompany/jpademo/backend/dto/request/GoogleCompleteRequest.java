package com.mycompany.jpademo.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @NotBlank(message = "Vui lòng chọn giới tính.")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Giới tính không hợp lệ.")
    private String gender;

    @NotNull(message = "Vui lòng nhập ngày sinh.")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố.")
    private String province;

    @NotBlank(message = "Vui lòng chọn quận/huyện.")
    private String district;

    @NotBlank(message = "Vui lòng nhập địa chỉ cư trú.")
    private String addressDetail;
}