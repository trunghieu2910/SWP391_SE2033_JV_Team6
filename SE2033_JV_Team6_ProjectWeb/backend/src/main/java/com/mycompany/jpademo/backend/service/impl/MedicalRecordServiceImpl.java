package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService; // Khớp chuẩn gói interfaces có chữ s của ông
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public List<MedicalRecordResponse> getPatientMedicalRecords(Integer patientID) {
        List<DiagnosisSession> sessions = sessionRepository.findByPatientPatientID(patientID);
        return sessions.stream().map(session -> {
            MedicalRecordResponse res = new MedicalRecordResponse();
            res.setSessionID(session.getSessionID());

            if (session.getCreatedAt() != null) {
                res.setCreatedAt(java.sql.Timestamp.valueOf(session.getCreatedAt()));
            }
            res.setStatus(session.getStatus());

            reviewRepository.findBySessionSessionID(session.getSessionID())
                    .ifPresent(r -> res.setFinalDiagnosis(r.getFinalDiagnosis()));
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public MedicalRecordDetailResponse getMedicalRecordDetail(Integer sessionID) {
        // 1. Kiểm tra ca khám (Session) tồn tại
        DiagnosisSession session = sessionRepository.findById(sessionID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã phiên khám này: " + sessionID));

        MedicalRecordDetailResponse detail = new MedicalRecordDetailResponse();

        // 2. Map thông tin chung của phiên khám (Cân nặng, chiều cao lấy trực tiếp từ DiagnosisSession của ông)
        detail.setSessionID(session.getSessionID());
        if (session.getCreatedAt() != null) {
            detail.setCreatedAt(java.sql.Timestamp.valueOf(session.getCreatedAt()));
        }
        detail.setStatus(session.getStatus());
        detail.setWeight(session.getWeight());
        detail.setHeight(session.getHeight());

        // 3. Map thông tin cá nhân Patient
        Patient patient = session.getPatient();
        if (patient != null) {
            detail.setPatientFirstName(patient.getFirstName());
            detail.setPatientLastName(patient.getLastName());
            if (patient.getDob() != null) {
                detail.setPatientDob(java.sql.Date.valueOf(patient.getDob()));
            }
            detail.setPatientGender(patient.getGender());
            detail.setPatientPhone(patient.getHealthInsurance()); // Bảo hiểm y tế
        }

        // 4. Cào danh sách triệu chứng từ SymptomDetails lồng trong phiên khám
        List<SymptomDetails> symptomDetailsList = symptomDetailsRepository.findByDiagnosisSessionSessionID(sessionID);
        if (symptomDetailsList != null && !symptomDetailsList.isEmpty()) {
            SymptomDetails firstDetail = symptomDetailsList.get(0);
            if (firstDetail.getSymptom() != null) {
                detail.setSymptomName(firstDetail.getSymptom().getSymptomName());
                detail.setSymptomDescription("Ghi nhận tại phiên khám");
            }
        }

        // 5. Cào phiếu xét nghiệm chuyên sâu (LabResult + LabResultParameter)
        List<LabResult> labResults = labResultRepository.findByDiagnosisSessionSessionID(sessionID);
        List<MedicalRecordDetailResponse.LabTestDTO> labTestDTOs = new ArrayList<>();

        if (labResults != null) {
            for (LabResult lr : labResults) {
                MedicalRecordDetailResponse.LabTestDTO labDTO = new MedicalRecordDetailResponse.LabTestDTO();
                labDTO.setTestName(lr.getTestType());
                if (lr.getCreatedAt() != null) {
                    labDTO.setTestedAt(java.sql.Timestamp.valueOf(lr.getCreatedAt()));
                }

                // Lấy chi tiết chỉ số đo đạc
                List<LabResultParameter> lrpList = labResultParameterRepository.findByLabResultLabResultID(lr.getLabResultID());
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

        // 6. Cào hình ảnh chụp chiếu (MedicalImage + MedicalImageDetails)
        List<MedicalImage> images = medicalImageRepository.findByDiagnosisSessionSessionID(sessionID);
        List<MedicalRecordDetailResponse.ImageDTO> imageDTOs = new ArrayList<>();

        if (images != null) {
            imageDTOs = images.stream().map(img -> {
                MedicalRecordDetailResponse.ImageDTO imgDTO = new MedicalRecordDetailResponse.ImageDTO();
                imgDTO.setImageType(img.getImageType());
                if (img.getCreatedAt() != null) {
                    imgDTO.setCreatedAt(java.sql.Timestamp.valueOf(img.getCreatedAt()));
                }

                List<MedicalImageDetails> midList = medicalImageDetailsRepository.findByMedicalImageMedicalImageID(img.getMedicalImageID());
                List<String> urls = midList.stream().map(MedicalImageDetails::getImageUrl).collect(Collectors.toList());
                imgDTO.setImageUrls(urls);
                return imgDTO;
            }).collect(Collectors.toList());
        }
        detail.setMedicalImages(imageDTOs);

        // 7. Cào kết luận cuối cùng chính thức của bác sĩ (Bảng Review)
        reviewRepository.findBySessionSessionID(sessionID).ifPresent(r -> {
            detail.setVerdict(r.getVerdict());
            detail.setFinalDiagnosis(r.getFinalDiagnosis());
            detail.setIcd10Code(r.getIcd10Code());
            detail.setTreatmentPlan(r.getTreatmentPlan());
            detail.setDoctorAdvice(r.getDoctorAdvice());
            detail.setNote(r.getNote());
        });

        return detail;
    }
}