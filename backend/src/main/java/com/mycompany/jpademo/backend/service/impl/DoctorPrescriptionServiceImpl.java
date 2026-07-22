package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateDoctorPrescriptionRequest;
import com.mycompany.jpademo.backend.dto.request.DoctorPrescriptionItemRequest;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.DrugRepository;
import com.mycompany.jpademo.backend.repository.PrescriptionRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.DoctorPrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorPrescriptionServiceImpl implements DoctorPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final DiagnosisSessionRepository sessionRepository;
    private final DrugRepository drugRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Drug> getActiveDrugs() {
        return drugRepository.findByStatus((byte) 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prescription> getPrescriptionBySessionId(Integer sessionId) {
        return prescriptionRepository.findBySessionSessionId(sessionId);
    }

    @Override
    @Transactional
    public Prescription savePrescription(Integer doctorId, CreateDoctorPrescriptionRequest request) {
        if (request == null || request.getSessionId() == null) {
            throw new BadRequestException("Mã ca chẩn đoán không được để trống.");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Đơn thuốc phải có ít nhất một loại thuốc.");
        }

        DiagnosisSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chẩn đoán #" + request.getSessionId()));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("Bạn không có quyền kê đơn thuốc cho ca chẩn đoán này.");
        }

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin bác sĩ."));

        // Lấy thông tin đơn thuốc cũ nếu đã tồn tại
        Prescription prescription = prescriptionRepository.findBySessionSessionId(request.getSessionId())
                .orElseGet(() -> Prescription.builder()
                        .prescriptionCode("RX" + request.getSessionId() + "-" + System.currentTimeMillis() % 100000)
                        .session(session)
                        .patient(session.getPatient())
                        .doctor(doctor)
                        .status((byte) 0) // 0: Chờ cấp phát
                        .prescriptionDate(LocalDateTime.now())
                        .build());

        // Cập nhật thông tin chẩn đoán từ Review nếu có
        if (session.getReview() != null && session.getReview().getDiseaseType() != null) {
            prescription.setDiagnosis(session.getReview().getDiseaseType().getName());
        } else {
            prescription.setDiagnosis("Ca chẩn đoán #" + request.getSessionId());
        }
        prescription.setNotes(request.getNotes());

        // Nếu đã có danh sách chi tiết đơn thuốc cũ -> xóa sạch để lưu lại danh sách mới
        if (prescription.getDetails() == null) {
            prescription.setDetails(new ArrayList<>());
        } else {
            prescription.getDetails().clear();
        }

        for (DoctorPrescriptionItemRequest itemReq : request.getItems()) {
            // Validate dữ liệu từng thuốc
            if (itemReq.getDrugId() == null) {
                throw new BadRequestException("Vui lòng chọn thuốc cho tất cả các dòng kê đơn.");
            }
            if (itemReq.getQuantityPrescribed() == null || itemReq.getQuantityPrescribed() <= 0) {
                throw new BadRequestException("Số lượng thuốc kê phải lớn hơn 0.");
            }
            if (itemReq.getInstruction() == null || itemReq.getInstruction().trim().isEmpty()) {
                throw new BadRequestException("Cách sử dụng thuốc không được để trống.");
            }

            Drug drug = drugRepository.findById(itemReq.getDrugId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc với ID: " + itemReq.getDrugId()));

            // Kiểm tra trạng thái thuốc: Không cho kê thuốc ngưng hoạt động (status == 0)
            if (drug.getStatus() == null || drug.getStatus() == 0) {
                throw new BadRequestException("Thuốc \"" + drug.getDrugName() + "\" (Mã: " + drug.getDrugCode() + ") đã ngưng sử dụng, không thể kê đơn.");
            }

            String baseUnitName = drug.getBaseUnit() != null ? drug.getBaseUnit().getUnitName() : "Đơn vị";

            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(prescription)
                    .drug(drug)
                    .dosePerTime(itemReq.getDosePerTime() != null ? itemReq.getDosePerTime() : BigDecimal.ONE)
                    .timesPerDay(itemReq.getTimesPerDay() != null ? itemReq.getTimesPerDay() : 1)
                    .daysOfTreatment(itemReq.getDaysOfTreatment() != null ? itemReq.getDaysOfTreatment() : 1)
                    .quantityPrescribed(itemReq.getQuantityPrescribed())
                    .dispenseUnit(baseUnitName)
                    .instruction(itemReq.getInstruction().trim())
                    .build();

            prescription.getDetails().add(detail);
        }

        log.info("Lưu đơn thuốc cho ca chẩn đoán #{}, tổng số thuốc: {}", request.getSessionId(), prescription.getDetails().size());
        return prescriptionRepository.save(prescription);
    }
}
