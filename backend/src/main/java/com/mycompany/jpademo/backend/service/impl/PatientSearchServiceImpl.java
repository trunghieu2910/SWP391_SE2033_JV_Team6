package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.PatientSearchResponse;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.service.interfaces.PatientSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientSearchServiceImpl implements PatientSearchService {
    private final PatientRepository patientRepository;

    @Override
    public List<PatientSearchResponse> searchPatients(String keyword) {
        List<Patient> patients;

        if (keyword == null || keyword.isBlank()) {
            patients = patientRepository.findAll();
        } else {
            // Tìm kiếm theo tên hoặc nationalID
            patients = patientRepository.findByUserFullNameContainingIgnoreCaseOrUserNationalIDContainingIgnoreCase(keyword, keyword);
        }

        return patients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Patient getPatientEntityById(Integer id) {
        return patientRepository.findById(id).orElse(null);
    }

    private PatientSearchResponse mapToResponse(Patient patient) {
        return PatientSearchResponse.builder()
                .patientId(patient.getPatientId())
                .fullName(patient.getUser().getFullName())
                .gender(patient.getGender())
                .dob(patient.getDob())
                .address(patient.getAddress())
                .nationalId(patient.getUser().getNationalID())
                .phoneNumber(patient.getUser().getPhoneNumber())
                .build();
    }
}
