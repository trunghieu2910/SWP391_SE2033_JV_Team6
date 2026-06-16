package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomResponse {
    private Integer symptomId;

    private String symptomName;

    private String menopauseStatus;

    private String symptomDuration;

    private Boolean symptomProgressing;
}
