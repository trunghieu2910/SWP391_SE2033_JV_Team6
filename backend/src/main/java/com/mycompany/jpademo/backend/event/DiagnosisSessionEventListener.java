package com.mycompany.jpademo.backend.event;

import com.mycompany.jpademo.backend.service.impl.SystemLogServiceImpl;
import com.mycompany.jpademo.backend.service.impl.DiagnosisSessionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiagnosisSessionEventListener {
    private final SystemLogServiceImpl systemLogService;

    @EventListener
    public void onDiagnosisSessionCreated(DiagnosisSessionServiceImpl.DiagnosisSessionCreatedEvent event) {
        log.info("Diagnosis session created - sessionId: {}, patientUserId: {}", event.getSessionId(), event.getPatientUserId());
        // Ghi log để FE biết có form cần fill
        systemLogService.logActivity("DiagnosisSession", event.getSessionId(), "PATIENT_NOTIFICATION",
                "Bệnh nhân được yêu cầu điền biểu mẫu triệu chứng");
    }

    @EventListener
    public void onSymptomFormSubmitted(DiagnosisSessionServiceImpl.SymptomFormSubmittedEvent event) {
        log.info("Symptom form submitted - sessionId: {}, userId: {}", event.getSessionId(), event.getUserId());
        // Ghi log để bác sĩ biết bệnh nhân đã submit
        systemLogService.logActivity("SymptomResult", event.getSessionId(), "DOCTOR_NOTIFICATION",
                "Bệnh nhân đã submit biểu mẫu triệu chứng, chờ bác sĩ xem xét");
    }
}
