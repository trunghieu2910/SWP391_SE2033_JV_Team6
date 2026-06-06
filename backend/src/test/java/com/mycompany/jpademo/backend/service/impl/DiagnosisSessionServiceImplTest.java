package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.entity.*;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import com.mycompany.jpademo.backend.enums.MedicalImageStatus;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosisSessionServiceImplTest {

    @Mock
    private DiagnosisSessionRepository diagnosisSessionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SymptomRepository symptomRepository;

    @Mock
    private SymptomResultRepository symptomResultRepository;

    @Mock
    private SymptomDetailsRepository symptomDetailsRepository;

    @Mock
    private LabResultRepository labResultRepository;

    @Mock
    private MedicalImageRepository medicalImageRepository;

    @Mock
    private SystemLogServiceImpl systemLogService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DiagnosisSessionServiceImpl diagnosisSessionService;

    private User doctor;
    private User patient;
    private Patient patientEntity;
    private DiagnosisSession diagnosisSession;
    private SymptomResult symptomResult;

    @BeforeEach
    void setUp() {
        // Setup doctor
        doctor = User.builder()
                .userId(1)
                .fullName("Dr. Nguyễn Văn Tùng")
                .build();

        // Setup patient
        patient = User.builder()
                .userId(2)
                .fullName("Phạm Thùy Linh")
                .nationalId("043678901234")
                .build();

        patientEntity = Patient.builder()
                .patientId(1)
                .user(patient)
                .gender("Female")
                .dob(LocalDate.of(2001, 2, 20))
                .address("Thanh Xuân, Hà Nội")
                .build();

        diagnosisSession = DiagnosisSession.builder()
                .sessionId(1)
                .user(doctor)
                .patient(patientEntity)
                .weight(58.0)
                .height(160.0)
                .status(DiagnosisSessionStatus.PENDING)
                .build();

        symptomResult = SymptomResult.builder()
                .symptomResultId(1)
                .diagnosisSession(diagnosisSession)
                .status(SymptomResultStatus.PENDING)
                .build();

        diagnosisSession.setSymptomResult(symptomResult);
    }

    @Test
    void testCreateSession_Success() {
        // Arrange
        CreateDiagnosisSessionRequest request = CreateDiagnosisSessionRequest.builder()
                .patientId(1)
                .weight(58.0)
                .height(160.0)
                .build();

        when(patientRepository.findById(1)).thenReturn(Optional.of(patientEntity));
        when(userRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(diagnosisSessionRepository.save(any(DiagnosisSession.class))).thenReturn(diagnosisSession);
        when(symptomResultRepository.save(any(SymptomResult.class))).thenReturn(symptomResult);
        when(labResultRepository.save(any(LabResult.class))).thenReturn(LabResult.builder().labResultId(1).build());
        when(medicalImageRepository.save(any(MedicalImage.class))).thenReturn(MedicalImage.builder().medicalImageId(1).build());

        // Act
        DiagnosisSessionResponse response = diagnosisSessionService.createSession(request, 1);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSessionId());
        assertEquals(1, response.getPatientId());
        assertEquals(58.0, response.getWeight());
        assertEquals(160.0, response.getHeight());
        assertEquals(DiagnosisSessionStatus.PENDING, response.getStatus());

        verify(patientRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);
        verify(diagnosisSessionRepository, times(1)).save(any(DiagnosisSession.class));
        verify(symptomResultRepository, times(1)).save(any(SymptomResult.class));
        verify(labResultRepository, times(1)).save(any(LabResult.class));
        verify(medicalImageRepository, times(1)).save(any(MedicalImage.class));
    }

    @Test
    void testCreateSession_PatientNotFound() {
        // Arrange
        CreateDiagnosisSessionRequest request = CreateDiagnosisSessionRequest.builder()
                .patientId(999)
                .weight(58.0)
                .height(160.0)
                .build();

        when(patientRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            diagnosisSessionService.createSession(request, 1);
        });

        verify(patientRepository, times(1)).findById(999);
    }

    @Test
    void testSubmitSymptomForm_PatientSuccess() {
        // Arrange
        SubmitSymptomFormRequest request = SubmitSymptomFormRequest.builder()
                .weight(60.0)
                .height(162.0)
                .menopauseStatus("ALREADY")
                .symptoms(Arrays.asList(1, 2, 3))
                .symptomDuration("MONTHS_1_3")
                .symptomProgressing(true)
                .build();

        Symptom symptom1 = Symptom.builder().symptomId(1).symptomName("Ra máu").build();
        Symptom symptom2 = Symptom.builder().symptomId(2).symptomName("Đau").build();
        Symptom symptom3 = Symptom.builder().symptomId(3).symptomName("Khí hư").build();

        when(diagnosisSessionRepository.findById(1)).thenReturn(Optional.of(diagnosisSession));
        when(symptomResultRepository.findByDiagnosisSessionSessionId(1)).thenReturn(Optional.of(symptomResult));
        when(symptomRepository.findAllById(Arrays.asList(1, 2, 3)))
                .thenReturn(Arrays.asList(symptom1, symptom2, symptom3));
        when(symptomDetailsRepository.saveAll(any())).thenReturn(List.of());
        when(symptomResultRepository.save(any(SymptomResult.class))).thenReturn(symptomResult);
        when(diagnosisSessionRepository.save(any(DiagnosisSession.class))).thenReturn(diagnosisSession);

        // Act
        SymptomResultResponse response = diagnosisSessionService.submitSymptomForm(1, request, 2, "ROLE_PATIENT");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSymptomResultId());
        assertEquals(SymptomResultStatus.PROCESSING, response.getStatus());

        verify(diagnosisSessionRepository, times(1)).findById(1);
        verify(symptomResultRepository, times(1)).findByDiagnosisSessionSessionId(1);
        verify(symptomRepository, times(1)).findAllById(Arrays.asList(1, 2, 3));
    }

    @Test
    void testSubmitSymptomForm_PatientUnauthorized() {
        // Arrange
        SubmitSymptomFormRequest request = SubmitSymptomFormRequest.builder()
                .weight(60.0)
                .height(162.0)
                .menopauseStatus("ALREADY")
                .symptoms(Arrays.asList(1, 2))
                .symptomDuration("MONTHS_1_3")
                .symptomProgressing(true)
                .build();

        when(diagnosisSessionRepository.findById(1)).thenReturn(Optional.of(diagnosisSession));

        // Act & Assert - Patient 99 không phải patient của session
        assertThrows(BadRequestException.class, () -> {
            diagnosisSessionService.submitSymptomForm(1, request, 99, "ROLE_PATIENT");
        });

        verify(diagnosisSessionRepository, times(1)).findById(1);
    }

    @Test
    void testGetSessionDetail_Success() {
        // Arrange
        when(diagnosisSessionRepository.findById(1)).thenReturn(Optional.of(diagnosisSession));

        // Act
        DiagnosisSessionResponse response = diagnosisSessionService.getSessionDetail(1);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSessionId());
        assertEquals(1, response.getPatientId());

        verify(diagnosisSessionRepository, times(1)).findById(1);
    }

    @Test
    void testGetSessionDetail_NotFound() {
        // Arrange
        when(diagnosisSessionRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            diagnosisSessionService.getSessionDetail(999);
        });

        verify(diagnosisSessionRepository, times(1)).findById(999);
    }

    @Test
    void testGetSymptomResult_Success() {
        // Arrange
        when(symptomResultRepository.findByDiagnosisSessionSessionId(1)).thenReturn(Optional.of(symptomResult));

        // Act
        SymptomResultResponse response = diagnosisSessionService.getSymptomResult(1);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSymptomResultId());
        assertEquals(SymptomResultStatus.PENDING, response.getStatus());

        verify(symptomResultRepository, times(1)).findByDiagnosisSessionSessionId(1);
    }
}
