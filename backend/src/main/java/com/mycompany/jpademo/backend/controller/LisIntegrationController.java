package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.repository.LabResultRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.impl.LisMockDataProvider;
import com.mycompany.jpademo.backend.service.interfaces.DoctorDiagnosisService;
import com.mycompany.jpademo.backend.service.interfaces.LisIntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Handles the "Lấy kết quả LIS" simulate action, triggered by a doctor
 * from the Thymeleaf UI. This project no longer integrates with a real
 * external LIS/HIS system — every lab result now comes exclusively from
 * the canned mock dataset in LisMockDataProvider.
 */
@Slf4j
@RestController
@RequestMapping("/doctor/lis")
@RequiredArgsConstructor
public class LisIntegrationController {

    private final LisIntegrationService lisIntegrationService;
    private final LisMockDataProvider lisMockDataProvider;
    private final DoctorDiagnosisService doctorDiagnosisService;
    private final LabResultRepository labResultRepository;

    /**
     * Simulates receiving LIS results for the given lab order using
     * canned mock data, on behalf of the currently logged-in doctor.
     * NEW: the test type is always read from the LabResult record itself
     * (never trusted from the client) so the mock results can never be
     * attached under a test type different from the one actually stored
     * for this order.
     */
    @PostMapping("/simulate/{sessionId}/{labResultId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void simulateFromUi(@PathVariable Integer sessionId,
                               @PathVariable Integer labResultId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) throws IOException {

        Integer doctorId = userDetails.getUser().getUserId();

        try {
            doctorDiagnosisService.verifyDoctorOwnsSession(doctorId, sessionId);
        } catch (UnauthorizedActionException | ResourceNotFoundException e) {
            httpRequest.getSession().setAttribute("flashError", e.getMessage());
            httpResponse.sendRedirect("/doctor/sessions");
            return;
        }

        // NEW: derive testType from the DB record instead of a client-submitted
        // request param — closes the "mismatched test type" data-integrity gap.
        LabResult labResult = labResultRepository.findById(labResultId).orElse(null);

        if (labResult == null
                || !labResult.getDiagnosisSession().getSessionId().equals(sessionId)) {
            httpRequest.getSession().setAttribute("flashError",
                    "Không tìm thấy xét nghiệm tương ứng trong phiên khám #" + sessionId);
            httpResponse.sendRedirect("/doctor/sessions/" + sessionId + "?openLab=true");
            return;
        }

        String testType = labResult.getTestType();
        List<LisResultRequest.TestResultItem> mockResults = lisMockDataProvider.getMockResults(testType);

        if (mockResults.isEmpty()) {
            httpRequest.getSession().setAttribute("flashError",
                    "Chưa có dữ liệu mẫu LIS cho loại xét nghiệm: \"" + testType + "\"");
            httpResponse.sendRedirect("/doctor/sessions/" + sessionId + "?openLab=true");
            return;
        }

        LisResultRequest lisRequest = new LisResultRequest();
        lisRequest.setLabResultId(labResultId);
        lisRequest.setTestResults(mockResults);

        try {
            lisIntegrationService.receiveLabResults(lisRequest);
            httpRequest.getSession().setAttribute("flashSuccess", "Đã nhận kết quả xét nghiệm thành công!");
        } catch (ResourceNotFoundException | UnauthorizedActionException | BadRequestException e) {
            httpRequest.getSession().setAttribute("flashError", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected error while simulating LIS result for labResultId={}, sessionId={}",
                    labResultId, sessionId, e);
            httpRequest.getSession().setAttribute("flashError",
                    "Đã xảy ra lỗi hệ thống khi xử lý xét nghiệm. Vui lòng thử lại sau.");
        }

        httpResponse.sendRedirect("/doctor/sessions/" + sessionId + "?openLab=true");
    }
}