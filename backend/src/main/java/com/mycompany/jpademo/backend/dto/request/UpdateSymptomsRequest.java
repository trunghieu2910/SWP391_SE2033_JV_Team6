package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSymptomsRequest {
    @NotNull(message = "Session ID must not be null")
    private Integer sessionId;

    @NotEmpty(message = "Symptom ID list cannot be empty")
    private List<Integer> symptomIds;

    @NotNull(message = "Menopause status must not be null")
    private String menopauseStatus;

    @NotNull(message = "Symptom duration must not be null")
    private String symptomDuration;

    @NotNull(message = "Symptom progressing must not be null")
    private Boolean symptomProgressing;
}
