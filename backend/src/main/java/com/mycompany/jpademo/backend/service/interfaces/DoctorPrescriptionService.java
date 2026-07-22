package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateDoctorPrescriptionRequest;
import com.mycompany.jpademo.backend.entity.Drug;
import com.mycompany.jpademo.backend.entity.Prescription;

import java.util.List;
import java.util.Optional;

public interface DoctorPrescriptionService {
    Prescription savePrescription(Integer doctorId, CreateDoctorPrescriptionRequest request);

    Optional<Prescription> getPrescriptionBySessionId(Integer sessionId);

    List<Drug> getActiveDrugs();
}
