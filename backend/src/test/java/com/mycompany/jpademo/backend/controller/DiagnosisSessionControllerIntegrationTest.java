package com.mycompany.jpademo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.jpademo.backend.dto.request.CreateDiagnosisSessionRequest;
import com.mycompany.jpademo.backend.dto.request.SubmitSymptomFormRequest;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.PatientSearchResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.enums.SymptomResultStatus;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import com.mycompany.jpademo.backend.service.interfaces.PatientSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DiagnosisSessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiagnosisSessionService diagnosisSessionService;

    @MockBean
    private PatientSearchService patientSearchService;

    private CustomUserDetails doctorUserDetails;
    private CustomUserDetails patientUserDetails;

    @BeforeEach
    void setUp() {
        // Mock doctor user
        doctorUserDetails = new CustomUserDetails(
                1,
                "dr_nguyen",
                "password",
                "Bác sĩ Nguyễn Văn Tùng",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR")),
                true,
                true,
                true,
                true,
                null // You may need to set a mock user entity
        );

        // Mock patient user
        patientUserDetails = new CustomUserDetails(
                2,
                "patient_linh",
                "password",
                "Phạm Thùy Linh",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")),
                true,
                true,
                true,
                true,
                null
        );
    }

    @Test
    void testSearchPatients_Success() throws Exception {
        // Arrange
        String keyword = "Linh";
        List<PatientSearchResponse> mockPatients = Arrays.asList(
                PatientSearchResponse.builder()
                        .patientId(1)
                        .fullName("Phạm Thùy Linh")
                        .gender("Female")
                        .dob(LocalDate.of(2001, 2, 20))
                        .address("Thanh Xuân, Hà Nội")
                        .nationalId("043678901234")
                        .build()
        );

        when(patientSearchService.searchPatients(keyword)).thenReturn(mockPatients);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/diagnosis-sessions/search-patients")
                        .param("keyword", keyword)
                        .with(user(doctorUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tìm kiếm bệnh nhân thành công"))
                .andExpect(jsonPath("$.data[0].fullName").value("Phạm Thùy Linh"))
                .andExpect(jsonPath("$.data[0].patientId").value(1));
    }

    @Test
    void testCreateSession_Success() throws Exception {
        // Arrange
        CreateDiagnosisSessionRequest request = CreateDiagnosisSessionRequest.builder()
                .patientId(1)
                .weight(58.0)
                .height(160.0)
                .build();

        DiagnosisSessionResponse mockResponse = DiagnosisSessionResponse.builder()
                .sessionId(1)
                .patientId(1)
                .patientName("Phạm Thùy Linh")
                .weight(58.0)
                .height(160.0)
                .status(DiagnosisSessionStatus.PENDING)
                .symptomResultStatus(SymptomResultStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(diagnosisSessionService.createSession(any(CreateDiagnosisSessionRequest.class), anyInt()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/diagnosis-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(doctorUserDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Tạo phiên khám thành công"))
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.patientName").value("Phạm Thùy Linh"));
    }

    @Test
    void testCreateSession_InvalidRequest() throws Exception {
        // Arrange - Missing required fields
        CreateDiagnosisSessionRequest request = CreateDiagnosisSessionRequest.builder()
                .patientId(1)
                // Missing weight and height
                .build();

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/diagnosis-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(doctorUserDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddPatientToSession_Success() throws Exception {
        // Arrange
        CreateDiagnosisSessionRequest request = CreateDiagnosisSessionRequest.builder()
                .patientId(1)
                .weight(58.0)
                .height(160.0)
                .build();

        DiagnosisSessionResponse mockResponse = DiagnosisSessionResponse.builder()
                .sessionId(1)
                .patientId(1)
                .patientName("Phạm Thùy Linh")
                .weight(58.0)
                .height(160.0)
                .status(DiagnosisSessionStatus.PENDING)
                .symptomResultStatus(SymptomResultStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(diagnosisSessionService.addPatientToSession(any(CreateDiagnosisSessionRequest.class), anyInt()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/diagnosis-sessions/add-patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(doctorUserDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Thêm bệnh nhân vào phiên khám thành công"));
    }

    @Test
    void testSubmitSymptomForm_PatientSuccess() throws Exception {
        // Arrange
        SubmitSymptomFormRequest request = SubmitSymptomFormRequest.builder()
                .weight(60.0)
                .height(162.0)
                .menopauseStatus("ALREADY")
                .symptoms(Arrays.asList(1, 2, 3))
                .symptomDuration("MONTHS_1_3")
                .symptomProgressing(true)
                .build();

        SymptomResultResponse mockResponse = SymptomResultResponse.builder()
                .symptomResultId(1)
                .sessionId(1)
                .status(SymptomResultStatus.PROCESSING)
                .symptomIds(Arrays.asList(1, 2, 3))
                .createdAt(LocalDateTime.now())
                .build();

        when(diagnosisSessionService.submitSymptomForm(anyInt(), any(SubmitSymptomFormRequest.class), anyInt(), anyString()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/diagnosis-sessions/1/symptom-result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(patientUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Submit biểu mẫu triệu chứng thành công"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    void testGetSessionDetail_Success() throws Exception {
        // Arrange
        DiagnosisSessionResponse mockResponse = DiagnosisSessionResponse.builder()
                .sessionId(1)
                .patientId(1)
                .patientName("Phạm Thùy Linh")
                .weight(58.0)
                .height(160.0)
                .status(DiagnosisSessionStatus.PENDING)
                .symptomResultStatus(SymptomResultStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(diagnosisSessionService.getSessionDetail(1)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/diagnosis-sessions/1")
                        .with(user(doctorUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.patientName").value("Phạm Thùy Linh"));
    }

    @Test
    void testGetSymptomResult_Success() throws Exception {
        // Arrange
        SymptomResultResponse mockResponse = SymptomResultResponse.builder()
                .symptomResultId(1)
                .sessionId(1)
                .status(SymptomResultStatus.PROCESSING)
                .symptomIds(Arrays.asList(1, 2, 3))
                .createdAt(LocalDateTime.now())
                .build();

        when(diagnosisSessionService.getSymptomResult(1)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/diagnosis-sessions/1/symptom-result")
                        .with(user(doctorUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.symptomResultId").value(1))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    void testUpdateSymptomForm_Success() throws Exception {
        // Arrange
        SubmitSymptomFormRequest request = SubmitSymptomFormRequest.builder()
                .weight(60.0)
                .height(162.0)
                .menopauseStatus("ALREADY")
                .symptoms(Arrays.asList(1, 2))
                .symptomDuration("MONTHS_1_3")
                .symptomProgressing(false)
                .build();

        SymptomResultResponse mockResponse = SymptomResultResponse.builder()
                .symptomResultId(1)
                .sessionId(1)
                .status(SymptomResultStatus.PROCESSING)
                .symptomIds(Arrays.asList(1, 2))
                .createdAt(LocalDateTime.now())
                .build();

        when(diagnosisSessionService.submitSymptomForm(anyInt(), any(SubmitSymptomFormRequest.class), anyInt(), anyString()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/diagnosis-sessions/1/symptom-result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(doctorUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật biểu mẫu triệu chứng thành công"));
    }
}
