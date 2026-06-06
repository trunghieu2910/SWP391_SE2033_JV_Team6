package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Autowired private DiagnosisSessionRepository sessionRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private SymptomDetailsRepository symptomDetailsRepository;
    @Autowired private LabResultRepository labResultRepository;
    @Autowired private LabResultParameterRepository labResultParameterRepository;
    @Autowired private MedicalImageRepository medicalImageRepository;
    @Autowired private MedicalImageDetailsRepository medicalImageDetailsRepository;
    @Autowired private SymptomResultRepository symptomResultRepository;

    // ===== MAP TỪ RAW SQL SANG DTO =====
    private MedicalRecordResponse mapToMedicalRecordResponse(Map<String, Object> row) {
        Boolean isShared = false;
        Object rawShared = row.get("isShared");
        if (rawShared instanceof Boolean) {
            isShared = (Boolean) rawShared;
        } else if (rawShared instanceof Number) {
            isShared = ((Number) rawShared).intValue() == 1;
        }

        return MedicalRecordResponse.builder()
                .id(((Number) row.get("id")).intValue())
                .patientName((String) row.get("patientName"))
                .diagnosis((String) row.get("diagnosis"))
                .visitDate((String) row.get("visitDate"))
                .symptoms((String) row.get("symptoms"))
                .prescription((String) row.get("prescription"))
                .doctorNotes((String) row.get("doctorNotes"))
                .isShared(isShared)
                .build();
    }

    // ===== LẤY DANH SÁCH (CÓ PHÂN TRANG) =====
    @Override
    public Page<MedicalRecordResponse> getMedicalRecords(String keyword, String status, Boolean isShared, Pageable pageable) {
        List<Map<String, Object>> rawRecords = sessionRepository.getMedicalRecords(keyword, status, isShared);

        List<MedicalRecordResponse> records = rawRecords.stream()
                .map(this::mapToMedicalRecordResponse)
                .collect(Collectors.toList());

        //  CHỐNG SẬP PHÂN TRANG
        int start = (int) pageable.getOffset();
        if (start >= records.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, records.size());
        }

        int end = Math.min((start + pageable.getPageSize()), records.size());
        List<MedicalRecordResponse> pageContent = records.subList(start, end);

        return new PageImpl<>(pageContent, pageable, records.size());
    }

    // ===== CHI TIẾT BỆNH ÁN (CÓ DATA MASKING) =====
    @Override
    public MedicalRecordDetailResponse getMedicalRecordDetail(Integer sessionID, boolean isPatient) {
        DiagnosisSession session = sessionRepository.findById(sessionID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã phiên khám: " + sessionID));

        MedicalRecordDetailResponse detail = new MedicalRecordDetailResponse();
        detail.setSessionID(session.getSessionId());
        if (session.getCreatedAt() != null) {
            detail.setCreatedAt(java.sql.Timestamp.valueOf(session.getCreatedAt()));
        }
        detail.setStatus(session.getStatus());
        detail.setWeight(session.getWeight());
        detail.setHeight(session.getHeight());

        Patient patient = session.getPatient();
        if (patient != null && patient.getUser() != null) {
            User patientUser = patient.getUser();
            detail.setPatientFirstName(patientUser.getFullName());
            detail.setPatientLastName("");
            if (patient.getDob() != null) {
                detail.setPatientDob(java.sql.Date.valueOf(patient.getDob()));
            }
            detail.setPatientGender(patient.getGender());
            detail.setPatientPhone(patientUser.getPhoneNumber());
        }

        // Lấy triệu chứng
        List<SymptomDetails> symptomDetailsList = symptomDetailsRepository.findBySymptomResult_DiagnosisSession_SessionId(sessionID);
        if (symptomDetailsList != null && !symptomDetailsList.isEmpty()) {
            SymptomDetails firstDetail = symptomDetailsList.get(0);
            if (firstDetail.getSymptom() != null) {
                detail.setSymptomName(firstDetail.getSymptom().getSymptomName());
                detail.setSymptomDescription("Ghi nhận tại phiên khám");
            }
        }

        // 🛠️ ĐÃ CHUYỂN VỊ TRÍ: Lấy SymptomResult (Đưa ra ngoài cho an toàn)
        symptomResultRepository.findByDiagnosisSession_SessionId(sessionID).ifPresent(sr -> {
            detail.setMenopauseStatus(sr.getMenopauseStatus());
            detail.setSymptomDuration(sr.getSymptomDuration());
            detail.setSymptomProgressing(sr.getSymptomProgressing());
        });

        // Lấy xét nghiệm
        List<LabResult> labResults = labResultRepository.findByDiagnosisSession_SessionId(sessionID);
        List<MedicalRecordDetailResponse.LabTestDTO> labTestDTOs = new ArrayList<>();
        if (labResults != null) {
            for (LabResult lr : labResults) {
                MedicalRecordDetailResponse.LabTestDTO labDTO = new MedicalRecordDetailResponse.LabTestDTO();
                labDTO.setTestName(lr.getTestType());
                if (lr.getCreatedAt() != null) {
                    labDTO.setTestedAt(java.sql.Timestamp.valueOf(lr.getCreatedAt()));
                }
                List<LabResultParameter> lrpList = labResultParameterRepository.findByLabResultLabResultId(lr.getLabResultId());
                List<MedicalRecordDetailResponse.ParamDTO> paramDTOs = lrpList.stream().map(lrp -> {
                    MedicalRecordDetailResponse.ParamDTO pDTO = new MedicalRecordDetailResponse.ParamDTO();
                    if (lrp.getParameter() != null) {
                        pDTO.setParamName(lrp.getParameter().getParameterName());
                        pDTO.setUnit(lrp.getParameter().getUnit());
                    }
                    pDTO.setValue(lrp.getValue());
                    return pDTO;
                }).collect(Collectors.toList());
                labDTO.setParameters(paramDTOs);
                labTestDTOs.add(labDTO);
            }
        }
        detail.setLabTests(labTestDTOs);

        // Lấy hình ảnh y tế
        List<MedicalImage> images = medicalImageRepository.findByDiagnosisSession_SessionId(sessionID);
        List<MedicalRecordDetailResponse.ImageDTO> imageDTOs = new ArrayList<>();
        if (images != null) {
            imageDTOs = images.stream().map(img -> {
                MedicalRecordDetailResponse.ImageDTO imgDTO = new MedicalRecordDetailResponse.ImageDTO();
                imgDTO.setImageType(img.getImageType());
                if (img.getCreatedAt() != null) {
                    imgDTO.setCreatedAt(java.sql.Timestamp.valueOf(img.getCreatedAt()));
                }
                List<MedicalImageDetails> midList = medicalImageDetailsRepository.findByMedicalImageMedicalImageId(img.getMedicalImageId());
                List<String> urls = midList.stream().map(MedicalImageDetails::getImageUrl).collect(Collectors.toList());
                imgDTO.setImageUrls(urls);
                return imgDTO;
            }).collect(Collectors.toList());
        }
        detail.setMedicalImages(imageDTOs);

        // DATA MASKING: Bệnh nhân chỉ thấy kết quả khi bác sĩ đã isShared = true
        final boolean canSeeDiagnosis = !(isPatient && (session.getIsShared() == null || !session.getIsShared()));

        reviewRepository.findByDiagnosisSessionSessionId(sessionID).ifPresent(r -> {
            if (canSeeDiagnosis) {
                detail.setVerdict(r.getVerdict());
                detail.setFinalDiagnosis(r.getFinalDiagnosis());
                detail.setIcd10Code(r.getIcd10Code());
                detail.setTreatmentPlan(r.getTreatmentPlan());
                detail.setDoctorAdvice(r.getDoctorAdvice());
                detail.setNote(r.getNote());
            } else {
                detail.setVerdict("BẢO MẬT");
                detail.setFinalDiagnosis("Đang chờ bác sĩ công bố...");
                detail.setIcd10Code("BẢO MẬT");
                detail.setTreatmentPlan("Chờ công bố");
                detail.setDoctorAdvice("Chờ công bố");
                detail.setNote("Bảo mật");
            }
        });

        return detail;
    }
}