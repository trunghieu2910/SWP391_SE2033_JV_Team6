package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PatientSearchResponse {
    private Integer patientId;
    private String fullName;
    private String gender;
    private LocalDate dob;
    private String address;
    private String nationalId;
    private String phoneNumber;
}
