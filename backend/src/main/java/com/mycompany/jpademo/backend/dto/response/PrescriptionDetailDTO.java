package com.mycompany.jpademo.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetailDTO {
    private Integer detailId;
    private Integer prescriptionId;
    private String prescriptionCode;
    private Integer drugId;
    private String drugName;
    private BigDecimal dosePerTime;
    private Integer timesPerDay;
    private Integer daysOfTreatment;
    private Integer quantityPrescribed;
    private Integer batchId;
    private String batchNumber;
    private LocalDate batchExpiryDate;
    private Integer quantityDispensed;
    private LocalDate actualExpiryDate;
    private String dispenseUnit;
    private String instruction;
    private LocalDateTime dispensedAt;
    private String dispensedByUser;
    private Integer dispensedByUserId;
    private String notes;
    private Integer patientId;
    private String patientName;
    private String patientCccd;
    private LocalDate patientDob;
    private Boolean isPending;
}
