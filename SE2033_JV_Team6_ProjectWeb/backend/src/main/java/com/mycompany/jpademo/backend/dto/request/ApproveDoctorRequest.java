package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveDoctorRequest {
    @NotNull(message = "Doctor ID cannot be null")
    private int doctorId;

    private Boolean sendEmail;

    private String approvalNote;
}
