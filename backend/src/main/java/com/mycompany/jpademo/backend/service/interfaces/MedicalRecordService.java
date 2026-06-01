package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import java.util.List;

public interface MedicalRecordService {
    // 1. Hàm lấy danh sách tất cả bệnh án (cho bác sĩ)
    List<MedicalRecordResponse> getAllMedicalRecords();

    // 2. Hàm xem danh sách các ca khám cũ của 1 bệnh nhân
    List<MedicalRecordResponse> getPatientMedicalRecords(Integer patientID);

    // 3. Hàm bóc tách chi tiết toàn bộ dữ liệu sạch của 1 ca khám cụ thể
    MedicalRecordDetailResponse getMedicalRecordDetail(Integer sessionID, boolean isPatient);

    // 4. Hàm bật/tắt công bố bệnh án
    void toggleRecordVisibility(Integer sessionId, Integer doctorId, boolean isShared);
}