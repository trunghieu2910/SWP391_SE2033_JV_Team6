package com.mycompany.jpademo.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private Integer sessionID;
    private Date createdAt;
    private String status;
    private String finalDiagnosis; // Lấy từ bảng Review ra hiển thị luôn cho tiện theo dõi
}