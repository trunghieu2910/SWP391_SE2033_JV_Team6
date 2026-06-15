package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.PatientSearchResponse;

import java.util.List;

public interface PatientSearchService {
    List<PatientSearchResponse> searchPatients(String keyword);
}
