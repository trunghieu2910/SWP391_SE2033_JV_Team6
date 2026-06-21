package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateSymptomsRequest;
import com.mycompany.jpademo.backend.dto.response.DoctorSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResponse;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor/sessions")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorDiagnosisController {
    private final DoctorDiagnosisService doctorDiagnosisService;

    @GetMapping
    public ResponseEntity<Page<DoctorSessionResponse>> getMySessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Integer doctorId = userDetails.getUser().getUserId();
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DoctorSessionResponse> sessions = doctorDiagnosisService.getSessionsByDoctor(doctorId, pageable, keyword);
        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/status")
    public ResponseEntity<String> updateSessionStatus(
            @Valid @RequestBody UpdateSessionStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer doctorId = userDetails.getUser().getUserId();
        doctorDiagnosisService.updateSessionStatus(doctorId, request);
        return ResponseEntity.ok("Diagnosis session status updated successfully.");
    }

    @PatchMapping("/share")
    public ResponseEntity<String> updateSessionShare(
            @Valid @RequestBody UpdateSessionShareRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer doctorId = userDetails.getUser().getUserId();
        doctorDiagnosisService.updateSessionShare(doctorId, request);
        return ResponseEntity.ok("Diagnosis session share status updated successfully.");
    }

    @GetMapping("/{sessionId}/symptoms")
    public ResponseEntity<List<SymptomResponse>> getSessionSymptoms(
            @PathVariable Integer sessionId) {
        List<SymptomResponse> symptoms = doctorDiagnosisService.getSessionSymptoms(sessionId);
        return ResponseEntity.ok(symptoms);
    }

    @PutMapping("/symptoms")
    public ResponseEntity<String> updateSessionSymptoms(
            @Valid @RequestBody UpdateSymptomsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer doctorId = userDetails.getUser().getUserId();
        doctorDiagnosisService.updateSessionSymptoms(doctorId, request);
        return ResponseEntity.ok("Clinical symptoms updated successfully.");
    }
}
