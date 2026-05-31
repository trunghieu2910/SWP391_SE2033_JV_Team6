package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private Integer userID;
    private Integer patientID;

    private String roleName;

    private String username;
    private String fullName;
    private String email;
    private String certificate;
    private String phoneNumber;
    private String status;
    private LocalDateTime createdAt;
    private String nationalID;

    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dob;
    private String address;
    private String healthInsurance;
}