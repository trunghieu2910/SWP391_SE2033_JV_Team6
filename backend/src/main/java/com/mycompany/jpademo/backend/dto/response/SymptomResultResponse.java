package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SymptomResultResponse {
    private Integer symptomResultId;
    private Integer sessionId;
    private SymptomResultStatus status;
    private LocalDateTime createdAt;
    private List<Integer> symptomIds;
    private String menopauseStatus;
    private String symptomDuration;
    private Boolean symptomProgressing;
}
