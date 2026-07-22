package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordResponse;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;
import com.mycompany.jpademo.backend.dto.response.DiagnosisSessionResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomDetailResponse;
import com.mycompany.jpademo.backend.dto.response.SymptomResultResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.SymptomDetails;
import com.mycompany.jpademo.backend.entity.SymptomResult;
import com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.repository.SymptomResultRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.MedicalRecordService;
import com.mycompany.jpademo.backend.service.interfaces.ProfileService;
import com.mycompany.jpademo.backend.service.interfaces.PdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientController {

    private final ProfileService profileService;
    private final MedicalRecordService medicalRecordService;
    private final PatientRepository patientRepository;
    private final DiagnosisSessionRepository sessionRepository;
    private final SymptomResultRepository symptomResultRepository;
    private final PdfService pdfService;
    private final com.mycompany.jpademo.backend.repository.PrescriptionRepository prescriptionRepository;

    @GetMapping("")
    public String redirectToHome() {
        return "redirect:/patient/home";
    }

    @GetMapping("/home")
    public String patientDashboard(Model model,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);

        DiagnosisSessionResponse activeSession = getActiveSession(patient);
        List<MedicalRecordResponse> recentRecords = getRecentMedicalRecords(patient.getPatientId(), 5);

        model.addAttribute("profile", profile);
        model.addAttribute("activeSession", activeSession);
        model.addAttribute("recentRecords", recentRecords);

        return "patient/dashboard";
    }

    @GetMapping("/medical-records")
    public String medicalRecords(Model model,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "9") int size) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);

        List<MedicalRecordResponse> allRecords = getRecentMedicalRecords(patient.getPatientId(), Integer.MAX_VALUE);
        allRecords.sort(Comparator.comparing(MedicalRecordResponse::getVisitDate, Comparator.nullsLast(Comparator.reverseOrder())));

        int start = Math.max(0, Math.min(page * size, allRecords.size()));
        int end = Math.min(start + size, allRecords.size());
        List<MedicalRecordResponse> pageRecords = new ArrayList<>();
        if (start < end) {
            pageRecords = allRecords.subList(start, end);
        }

        int totalPages = size <= 0 ? 1 : (int) Math.ceil((double) allRecords.size() / size);
        boolean hasPrev = page > 0;
        boolean hasNext = page < totalPages - 1;

        model.addAttribute("profile", profile);
        model.addAttribute("records", pageRecords);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);

        return "patient/medical-records";
    }

    @GetMapping("/medical-record/{id}")
    public String medicalRecordDetail(Model model,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Integer id) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);

        DiagnosisSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bệnh án: " + id));
        if (!session.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập hồ sơ này.");
        }

        MedicalRecordDetailResponse record = medicalRecordService.getMedicalRecordDetail(id, true);

        model.addAttribute("profile", profile);
        model.addAttribute("record", record);
        model.addAttribute("prescription", prescriptionRepository.findBySessionSessionId(id).orElse(null));

        return "patient/medical-record-detail";
    }

    @GetMapping("/medical-record/{id}/export")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<byte[]> exportMedicalRecordPdf(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Patient patient = getPatient(userDetails);

        DiagnosisSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bệnh án: " + id));

        if (!session.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập hồ sơ này.");
        }

        // Bệnh nhân chỉ xuất được bản có che/mask chẩn đoán nếu bác sĩ chưa công bố
        MedicalRecordDetailResponse record = medicalRecordService.getMedicalRecordDetail(id, true);
        byte[] pdfBytes = pdfService.generateMedicalRecordPdf(record);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "medical_record_S" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/new-session")
    public String newSession(Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(defaultValue = "false") boolean viewForm) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        Patient patient = getPatient(userDetails);
        DiagnosisSessionResponse activeSession = getActiveSession(patient);

        if (activeSession == null) {
            return "redirect:/patient/home";
        }

        model.addAttribute("profile", profile);
        model.addAttribute("activeSession", activeSession);
        model.addAttribute("viewForm", viewForm);

        return "patient/new-session";
    }

    private DiagnosisSessionResponse getActiveSession(Patient patient) {
        return sessionRepository.findByPatientPatientIdWithDetails(patient.getPatientId()).stream()
                .filter(s -> s.getStatus() != null && s.getStatus() != DiagnosisSessionStatus.COMPLETED)
                .max(Comparator.comparing(DiagnosisSession::getCreatedAt))
                .map(session -> {
                    SymptomResult symptomResult = session.getSymptomResult();
                    if (symptomResult != null) {
                        symptomResult = symptomResultRepository.findBySessionIdWithDetails(session.getSessionId())
                                .orElse(symptomResult);
                    }
                    return DiagnosisSessionResponse.builder()
                            .sessionId(session.getSessionId())
                            .patientId(session.getPatient().getPatientId())
                            .patientName(session.getPatient().getUser().getFullName())
                            .status(session.getStatus())
                            .symptomResultStatus(symptomResult != null ? symptomResult.getStatus() : null)
                            .clinicalInputMode(session.getClinicalInputMode())
                            .createdAt(session.getCreatedAt())
                            .weight(session.getWeight())
                            .height(session.getHeight())
                            .symptomResult(symptomResult != null ? mapSymptomResult(symptomResult) : null)
                            .build();
                })
                .orElse(null);
    }

    private SymptomResultResponse mapSymptomResult(SymptomResult symptomResult) {
        if (symptomResult == null) {
            return null;
        }

        List<SymptomDetailResponse> symptomDetails = symptomResult.getSymptomDetails() != null
                ? symptomResult.getSymptomDetails().stream()
                .map(detail -> SymptomDetailResponse.builder()
                        .symptomDetailId(detail.getSymptomDetailsId())
                        .symptomId(detail.getSymptom().getSymptomId())
                        .symptomName(detail.getSymptom().getSymptomName())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        List<Integer> symptomIds = symptomDetails.stream()
                .map(SymptomDetailResponse::getSymptomId)
                .collect(Collectors.toList());

        return SymptomResultResponse.builder()
                .symptomResultId(symptomResult.getSymptomResultId())
                .sessionId(symptomResult.getDiagnosisSession().getSessionId())
                .status(symptomResult.getStatus())
                .createdAt(symptomResult.getCreatedAt())
                .symptomIds(symptomIds)
                .menopauseStatus(symptomResult.getMenopauseStatus())
                .symptomDuration(symptomResult.getSymptomDuration())
                .symptomProgressing(symptomResult.getSymptomProgressing())
                .symptomDetails(symptomDetails)
                .build();
    }

    @GetMapping("/profile")
    public String profilePage(Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "success", required = false) boolean success) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        UpdateProfileRequest profileForm = UpdateProfileRequest.builder()
                .username(profile.getUsername())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .nationalID(profile.getNationalID())
                .gender(profile.getGender())
                .dob(profile.getDob())
                .address(profile.getAddress())
                .build();

        model.addAttribute("profile", profile);
        model.addAttribute("profileForm", profileForm);
        model.addAttribute("success", success);
        model.addAttribute("today", LocalDate.now());

        return "patient/profile";
    }

    @PostMapping("/profile")
    @Transactional
    public String saveProfile(@Valid @ModelAttribute("profileForm") UpdateProfileRequest profileForm,
                               BindingResult bindingResult,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("profileForm", profileForm);
            return "patient/profile";
        }

        profileService.updateProfile(userDetails.getUsername(), profileForm);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công.");
        return "redirect:/patient/profile?success=true";
    }

    private Patient getPatient(CustomUserDetails userDetails) {
        return patientRepository.findByUser(userDetails.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy patient cho user hiện tại."));
    }

    private List<MedicalRecordResponse> getRecentMedicalRecords(Integer patientId, int limit) {
        List<Map<String, Object>> rawRecords = sessionRepository.findMedicalRecordsByPatientId(patientId);
        List<MedicalRecordResponse> records = new ArrayList<>();
        for (Map<String, Object> raw : rawRecords) {
            records.add(mapToMedicalRecordResponse(raw));
        }
        records.sort(Comparator.comparing(MedicalRecordResponse::getVisitDate, Comparator.nullsLast(Comparator.reverseOrder())));
        if (limit > 0 && records.size() > limit) {
            return records.subList(0, limit);
        }
        return records;
    }

    private MedicalRecordResponse mapToMedicalRecordResponse(Map<String, Object> row) {
        Boolean isShared = false;
        Object rawShared = row.get("isShared");
        if (rawShared instanceof Boolean booleanValue) {
            isShared = booleanValue;
        } else if (rawShared instanceof Number numberValue) {
            isShared = numberValue.intValue() == 1;
        }

        return MedicalRecordResponse.builder()
                .id(row.get("id") != null ? ((Number) row.get("id")).intValue() : null)
                .patientName((String) row.get("patientName"))
                .diagnosis((String) row.get("diagnosis"))
                .visitDate((java.util.Date) row.get("visitDate"))
                .symptoms((String) row.get("symptoms"))
                .prescription((String) row.get("prescription"))
                .doctorNotes((String) row.get("doctorNotes"))
                .isShared(isShared)
                .nationalID((String) row.get("nationalID"))
                .gender((String) row.get("gender"))
                .doctorFullName((String) row.get("doctorFullName"))
                .build();
    }
}
