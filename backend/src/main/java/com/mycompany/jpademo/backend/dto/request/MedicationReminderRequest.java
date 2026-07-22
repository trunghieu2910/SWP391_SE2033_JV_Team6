package com.mycompany.jpademo.backend.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationReminderRequest {

    @NotBlank(message = "Ghi chú không được để trống")
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime scheduledTime;

    @DateTimeFormat(pattern = "HH:mm")
    private java.util.List<LocalTime> scheduledTimes;
}
