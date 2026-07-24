package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO that represents a grouped prescription (1 row per prescriptionCode).
 * Each row shows the prescription header info, and its detail items.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionSummaryDTO {
    private Integer prescriptionId;
    private String prescriptionCode;
    private String patientName;
    private LocalDateTime prescriptionDate;
    private String diagnosis;
    private com.mycompany.jpademo.backend.enums.PrescriptionStatus status;
    private Integer totalItems;
    private Integer pendingItems;
    private Boolean isPending;
    /** The list of drug detail items for this prescription */
    private List<PrescriptionDetailDTO> details;
}
