package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;

public interface PdfService {
    /**
     * Generates a PDF byte array representing the medical record detail.
     *
     * @param record the detailed medical record response
     * @param isPatientView true if generating for patient, false for doctor
     * @return PDF content as a byte array
     */
    byte[] generateMedicalRecordPdf(MedicalRecordDetailResponse record, boolean isPatientView);
}
