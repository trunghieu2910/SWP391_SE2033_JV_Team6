package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.response.PatientSearchResponse;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientSearchServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientSearchServiceImpl patientSearchService;

    private User patientUser1;
    private User patientUser2;
    private Patient patient1;
    private Patient patient2;

    @BeforeEach
    void setUp() {
        patientUser1 = User.builder()
                .userId(1)
                .fullName("Phạm Thùy Linh")
                .nationalId("043678901234")
                .build();

        patientUser2 = User.builder()
                .userId(2)
                .fullName("Lê Minh Hoàng")
                .nationalId("034567890123")
                .build();

        patient1 = Patient.builder()
                .patientId(1)
                .user(patientUser1)
                .gender("Female")
                .dob(LocalDate.of(2001, 2, 20))
                .address("Thanh Xuân, Hà Nội")
                .build();

        patient2 = Patient.builder()
                .patientId(2)
                .user(patientUser2)
                .gender("Male")
                .dob(LocalDate.of(1995, 8, 15))
                .address("Cầu Giấy, Hà Nội")
                .build();
    }

    @Test
    void testSearchPatients_ByFullName() {
        // Arrange
        String keyword = "Linh";
        when(patientRepository.findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword))
                .thenReturn(Arrays.asList(patient1));

        // Act
        List<PatientSearchResponse> results = patientSearchService.searchPatients(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Phạm Thùy Linh", results.get(0).getFullName());
        assertEquals(1, results.get(0).getPatientId());

        verify(patientRepository, times(1))
                .findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword);
    }

    @Test
    void testSearchPatients_ByNationalId() {
        // Arrange
        String keyword = "043678901234";
        when(patientRepository.findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword))
                .thenReturn(Arrays.asList(patient1));

        // Act
        List<PatientSearchResponse> results = patientSearchService.searchPatients(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("043678901234", results.get(0).getNationalId());

        verify(patientRepository, times(1))
                .findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword);
    }

    @Test
    void testSearchPatients_MultipleResults() {
        // Arrange
        String keyword = "Hà";
        when(patientRepository.findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword))
                .thenReturn(Arrays.asList(patient1, patient2));

        // Act
        List<PatientSearchResponse> results = patientSearchService.searchPatients(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Phạm Thùy Linh", results.get(0).getFullName());
        assertEquals("Lê Minh Hoàng", results.get(1).getFullName());

        verify(patientRepository, times(1))
                .findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword);
    }

    @Test
    void testSearchPatients_NoResults() {
        // Arrange
        String keyword = "NonExistent";
        when(patientRepository.findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword))
                .thenReturn(Arrays.asList());

        // Act
        List<PatientSearchResponse> results = patientSearchService.searchPatients(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());

        verify(patientRepository, times(1))
                .findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword);
    }

    @Test
    void testSearchPatients_NullKeyword() {
        // Arrange
        when(patientRepository.findAll())
                .thenReturn(Arrays.asList(patient1, patient2));

        // Act
        List<PatientSearchResponse> results = patientSearchService.searchPatients(null);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());

        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testSearchPatients_BlankKeyword() {
        // Arrange
        when(patientRepository.findAll())
                .thenReturn(Arrays.asList(patient1, patient2));

        // Act
        List<PatientSearchResponse> results = patientSearchService.searchPatients("   ");

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());

        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testSearchPatients_ResponseMapping() {
        // Arrange
        String keyword = "Linh";
        when(patientRepository.findByUserFullNameContainingIgnoreCaseOrUserNationalIdContainingIgnoreCase(keyword, keyword))
                .thenReturn(Arrays.asList(patient1));

        // Act
        List<PatientSearchResponse> results = patientSearchService.searchPatients(keyword);

        // Assert
        PatientSearchResponse response = results.get(0);
        assertEquals(1, response.getPatientId());
        assertEquals("Phạm Thùy Linh", response.getFullName());
        assertEquals("Female", response.getGender());
        assertEquals(LocalDate.of(2001, 2, 20), response.getDob());
        assertEquals("Thanh Xuân, Hà Nội", response.getAddress());
        assertEquals("043678901234", response.getNationalId());
    }
}
