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
public class RejectDoctorRequest {
    @NotNull(message = "Doctor ID cannot be null")
    private int doctorId;

    private String rejectNote;
}
