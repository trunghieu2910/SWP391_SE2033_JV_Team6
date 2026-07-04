package com.mycompany.jpademo.backend.dto.request;

import com.mycompany.jpademo.backend.aop.interfaces.LoggableTarget;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateDoctorRequest implements LoggableTarget {
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

    private Integer doctorId;

    @Override
    public Integer getTargetId() {
        return doctorId;
    }
}
