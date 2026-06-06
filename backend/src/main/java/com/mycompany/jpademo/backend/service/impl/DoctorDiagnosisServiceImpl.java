package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.*;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorDiagnosisServiceImpl implements DoctorDiagnosisService {

    private final DiagnosisSessionRepository sessionRepository;
    private final SymptomRepository symptomRepository;
    private final SymptomResultRepository symptomResultRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSessionResponse> getSessionsByDoctor(Integer doctorId,
                                                           Pageable pageable,
                                                           String keyword) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<DiagnosisSession> sessionPage = sessionRepository.searchByDoctorWithKeyword(doctorId, normalizedKeyword, pageable);

        return sessionPage.map(session -> DoctorSessionResponse.builder()
                .sessionId(session.getSessionId())
                .patientId(session.getPatient().getPatientId())
                .fullName(session.getPatient().getUser() != null ? session.getPatient().getUser().getFullName() : "Unknown")
                .status(session.getStatus())
                .isShared(session.getIsShared())
                .createdAt(session.getCreatedAt())
                .build());
    }

    @Override
    @Transactional
    public void updateSessionStatus(Integer doctorId, UpdateSessionStatusRequest request) {
        Integer sessionId = request.getSessionId();
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis session not found with ID: " + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("You are not authorized to update this session.");
        }

        DiagnosisSessionStatus newStatus;
        try {
            newStatus = DiagnosisSessionStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + request.getStatus() + ". Must be PENDING, PROCESSING, COMPLETED, or FAILED.");
        }

        session.setStatus(newStatus);
        sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void updateSessionShare(Integer doctorId, UpdateSessionShareRequest request) {
        Integer sessionId = request.getSessionId();
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis session not found with ID: " + sessionId));

        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException("You are not authorized to update this session.");
        }

        session.setIsShared(request.getIsShared());
        sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SymptomResponse> getSessionSymptoms(Integer sessionId) {

        SymptomResult symptomResult = symptomResultRepository
                .findByDiagnosisSessionSessionId(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No clinical symptoms recorded for session ID: "
                                        + sessionId));
        String menopauseStatus = symptomResult.getMenopauseStatus() != null
                ? symptomResult.getMenopauseStatus().toString()
                : null;
        String symptomDuration = symptomResult.getSymptomDuration();
        Boolean symptomProgressing = symptomResult.getSymptomProgressing();

        return symptomResult.getSymptomDetails()
                .stream()
                .map(detail -> SymptomResponse.builder()
                        .symptomId(detail.getSymptom().getSymptomId())
                        .symptomName(detail.getSymptom().getSymptomName())
                        .menopauseStatus(menopauseStatus)
                        .symptomDuration(symptomDuration)
                        .symptomProgressing(symptomProgressing)
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void updateSessionSymptoms(Integer doctorId, UpdateSymptomsRequest request) {
        Integer sessionId = request.getSessionId();
        DiagnosisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Diagnosis session not found with ID: "
                                        + sessionId));
        if (!session.getUser().getUserId().equals(doctorId)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to update this session.");
        }
        if (request.getSymptomIds() == null
                || request.getSymptomIds().isEmpty()) {
            throw new BadRequestException(
                    "At least one symptom is required.");
        }
        SymptomResult symptomResult = symptomResultRepository
                .findByDiagnosisSessionSessionId(sessionId)
                .orElseGet(() -> {
                    SymptomResult result = new SymptomResult();
                    result.setDiagnosisSession(session);
                    result.setStatus(SymptomResultStatus.COMPLETED);
                    result.setCreatedAt(LocalDateTime.now());
                    result.setSymptomDetails(new ArrayList<>());
                    return result;
                });
        if (request.getMenopauseStatus() != null) {
            symptomResult.setMenopauseStatus(request.getMenopauseStatus());
        } else if (symptomResult.getMenopauseStatus() == null) {
            symptomResult.setMenopauseStatus(false);
        }
        if (request.getSymptomDuration() != null) {
            symptomResult.setSymptomDuration(request.getSymptomDuration());
        } else if (symptomResult.getSymptomDuration() == null) {
            symptomResult.setSymptomDuration("");
        }
        if (request.getSymptomProgressing() != null) {
            symptomResult.setSymptomProgressing(request.getSymptomProgressing());
        } else if (symptomResult.getSymptomProgressing() == null) {
            symptomResult.setSymptomProgressing(false);
        }
        List<Symptom> symptoms = symptomRepository.findAllById(request.getSymptomIds());
        if (symptoms.size() != request.getSymptomIds().size()) {
            throw new ResourceNotFoundException(
                    "One or more symptoms not found.");
        }
        symptomResult.getSymptomDetails().clear();
        for (Symptom symptom : symptoms) {
            SymptomDetails detail = new SymptomDetails();
            detail.setSymptomResult(symptomResult);
            detail.setSymptom(symptom);
            symptomResult.getSymptomDetails().add(detail);
        }
        symptomResultRepository.save(symptomResult);
    }
}