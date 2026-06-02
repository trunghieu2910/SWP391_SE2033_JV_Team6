package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class OtpVerificationRequest {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập.")
    private String userName;

    @NotBlank(message = "Vui lòng nhập mã OTP.")
    private String otp;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
