package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
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

        // Convert status từ String sang DiagnosisSessionStatus enum
        DiagnosisSessionStatus status = null;

        if (row.get("status") != null) {
            status = DiagnosisSessionStatus.valueOf(
                    row.get("status").toString()
            );
        }
        return MedicalRecordResponse.builder()
                .id(((Number) row.get("id")).intValue())
                .patientName((String) row.get("patientName"))
                .diagnosis((String) row.get("diagnosis"))
                .visitDate((Date) row.get("visitDate"))
                .symptoms((String) row.get("symptoms"))
                .prescription((String) row.get("prescription"))
                .doctorNotes((String) row.get("doctorNotes"))
                .nationalID((String) row.get("nationalID"))
                .gender((String) row.get("gender"))
                .isShared(isShared)
                .status(status)
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

    // ===== DANH SÁCH CHO BÁC SĨ =====
    @Override
    public List<MedicalRecordResponse> getAllMedicalRecords() {
        List<Map<String, Object>> rawRecords = sessionRepository.findAllMedicalRecords();
        return rawRecords.stream()
                .map(this::mapToMedicalRecordResponse)
                .collect(Collectors.toList());
    }

    // ===== DANH SÁCH CHO BỆNH NHÂN =====
    @Override
    public List<MedicalRecordResponse> getPatientMedicalRecords(Integer patientID) {
        List<Map<String, Object>> rawRecords = sessionRepository.findMedicalRecordsByPatientId(patientID);
        return rawRecords.stream()
                .map(this::mapToMedicalRecordResponse)
                .collect(Collectors.toList());
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
        detail.setIsShared(session.getIsShared());

        // Patient Info
        Patient patient = session.getPatient();
        if (patient != null && patient.getUser() != null) {
            User patientUser = patient.getUser();
            detail.setPatientFullName(patientUser.getFullName());
            detail.setPatientFirstName(patientUser.getFullName());
            detail.setPatientLastName("");
            detail.setPatientNationalID(patientUser.getNationalID());
            detail.setPatientPhone(patientUser.getPhoneNumber());
            if (patient.getDob() != null) {
                detail.setPatientDob(java.sql.Date.valueOf(patient.getDob()));
            }
            detail.setPatientGender(patient.getGender());
            detail.setPatientAddress(patient.getAddress());
        }

        // Doctor Info
        if (session.getUser() != null) {
            detail.setDoctorFullName(session.getUser().getFullName());
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

        // Lấy SymptomResult
        symptomResultRepository.findByDiagnosisSession_SessionId(sessionID).ifPresent(sr -> {
            detail.setSymptomResultID(sr.getSymptomResultId());
            detail.setSymptomResultStatus(sr.getStatus() != null ? sr.getStatus().toString() : null);
            detail.setMenopauseStatus(sr.getMenopauseStatus());
            detail.setSymptomDuration(sr.getSymptomDuration());
            detail.setSymptomProgressing(sr.getSymptomProgressing());
            
            // Lấy danh sách symptoms từ symptomDetails
            if (sr.getSymptomDetails() != null && !sr.getSymptomDetails().isEmpty()) {
                List<MedicalRecordDetailResponse.SymptomDTO> symptomDTOs = sr.getSymptomDetails().stream()
                        .map(sd -> {
                            MedicalRecordDetailResponse.SymptomDTO dto = new MedicalRecordDetailResponse.SymptomDTO();
                            if (sd.getSymptom() != null) {
                                dto.setSymptomID(sd.getSymptom().getSymptomId());
                                dto.setSymptomName(sd.getSymptom().getSymptomName());
                            }
                            return dto;
                        })
                        .collect(Collectors.toList());
                detail.setSymptoms(symptomDTOs);
            }
        });

        // Lấy xét nghiệm
        List<LabResult> labResults = labResultRepository.findByDiagnosisSessionSessionId(sessionID);
        List<MedicalRecordDetailResponse.LabTestDTO> labTestDTOs = new ArrayList<>();
        if (labResults != null && !labResults.isEmpty()) {
            for (LabResult lr : labResults) {
                MedicalRecordDetailResponse.LabTestDTO labDTO = new MedicalRecordDetailResponse.LabTestDTO();
                labDTO.setLabResultID(lr.getLabResultId());
                labDTO.setTestName(lr.getTestType());
                labDTO.setStatus(lr.getStatus() != null ? lr.getStatus().toString() : null);
                if (lr.getCreatedAt() != null) {
                    labDTO.setTestedAt(java.sql.Timestamp.valueOf(lr.getCreatedAt()));
                }
                List<LabResultParameter> lrpList = labResultParameterRepository.findByLabResultLabResultId(lr.getLabResultId());
                List<MedicalRecordDetailResponse.ParamDTO> paramDTOs = lrpList.stream().map(lrp -> {
                    MedicalRecordDetailResponse.ParamDTO pDTO = new MedicalRecordDetailResponse.ParamDTO();
                    pDTO.setLabResultParameterID(lrp.getLabResultParameterId());
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
        if (images != null && !images.isEmpty()) {
            imageDTOs = images.stream().map(img -> {
                MedicalRecordDetailResponse.ImageDTO imgDTO = new MedicalRecordDetailResponse.ImageDTO();
                imgDTO.setMedicalImageID(img.getMedicalImageId());
                imgDTO.setImageType(img.getImageType());
                imgDTO.setStatus(img.getStatus() != null ? img.getStatus().toString() : null);
                if (img.getCreatedAt() != null) {
                    imgDTO.setCreatedAt(java.sql.Timestamp.valueOf(img.getCreatedAt()));
                }
                List<MedicalImageDetails> midList = medicalImageDetailsRepository.findByMedicalImageMedicalImageId(img.getMedicalImageId());
                List<MedicalRecordDetailResponse.ImageDetailDTO> detailDTOs = midList.stream()
                        .map(mid -> {
                            MedicalRecordDetailResponse.ImageDetailDTO detailDTO = new MedicalRecordDetailResponse.ImageDetailDTO();
                            detailDTO.setImageID(mid.getImageId());
                            detailDTO.setImageUrl(mid.getImageUrl());
                            if (mid.getUploadedAt() != null) {
                                detailDTO.setUploadedAt(java.sql.Timestamp.valueOf(mid.getUploadedAt()));
                            }
                            return detailDTO;
                        })
                        .collect(Collectors.toList());
                imgDTO.setDetails(detailDTOs);
                return imgDTO;
            }).collect(Collectors.toList());
        }
        detail.setMedicalImages(imageDTOs);

        // DATA MASKING: Bệnh nhân chỉ thấy kết quả khi bác sĩ đã isShared = true
        final boolean canSeeDiagnosis = !(isPatient && (session.getIsShared() == null || !session.getIsShared()));

        reviewRepository.findByDiagnosisSessionSessionId(sessionID).ifPresent(r -> {
            detail.setReviewID(r.getReviewId());
            if (r.getReviewedAt() != null) {
                detail.setReviewedAt(java.sql.Timestamp.valueOf(r.getReviewedAt()));
            }
            if (r.getUser() != null) {
                detail.setReviewedByDoctorName(r.getUser().getFullName());
            }

            if (canSeeDiagnosis) {
                detail.setFinalDiagnosis(r.getFinalDiagnosis());
                detail.setTreatmentPlan(r.getTreatmentPlan());
                detail.setDoctorAdvice(r.getDoctorAdvice());
                detail.setNote(r.getNote());
            } else {
                detail.setFinalDiagnosis("Đang chờ bác sĩ công bố...");
                detail.setTreatmentPlan("Chờ công bố");
                detail.setDoctorAdvice("Chờ công bố");
                detail.setNote("Bảo mật");
            }
        });

        return detail;
    }

    @Override
    public void updateMedicalRecordVisibility(Integer sessionID, Boolean isShared) {
        DiagnosisSession session = sessionRepository.findById(sessionID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã phiên khám: " + sessionID));
        
        session.setIsShared(isShared);
        sessionRepository.save(session);
    }
}