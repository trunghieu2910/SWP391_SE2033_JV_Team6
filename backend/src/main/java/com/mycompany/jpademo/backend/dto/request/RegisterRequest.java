package com.mycompany.jpademo.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class RegisterRequest {

    @NotBlank(message = "Vui lòng nhập họ và tên.")
    private String fullName;

    @NotBlank(message = "Vui lòng chọn giới tính.")
    private String gender;

    @NotNull(message = "Vui lòng nhập ngày sinh.")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotBlank(message = "Vui lòng nhập địa chỉ.")
    private String address;

    @NotBlank(message = "Vui lòng nhập số CMND/CCCD.")
    @Pattern(regexp = "^\\d{12}$", message = "Số CMND/CCCD phải đúng 12 chữ số.")
    private String nationalID;

    @NotBlank(message = "Vui lòng nhập số điện thoại.")
    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải đúng 10 chữ số.")
    private String phoneNumber;

    @NotBlank(message = "Vui lòng nhập tên đăng nhập.")
    private String userName;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @Size(min = 8, message = "Mật khẩu phải ít nhất 8 ký tự.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$",
            message = "Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt."
    )
    private String password;

    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không hợp lệ.")
    private String email;

    private String confirmPassword;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNationalID() {
        return nationalID;
    }

    public void setNationalID(String nationalID) {
        this.nationalID = nationalID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
