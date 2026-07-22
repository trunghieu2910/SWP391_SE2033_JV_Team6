package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.DoctorWorkloadResponse;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.DiagnosisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/receptionist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionistDoctorWorkloadController {

    private final DiagnosisSessionService diagnosisSessionService;
    private final UserRepository userRepository;

    // ==================== Màn hình 1: Danh sách tải bác sĩ ====================
    // [Nguyen The Hieu]: Bước 4 - Controller: Nhận request GET /receptionist/doctor-workload
    @GetMapping("/doctor-workload")
    public String doctorWorkloadPage(Model model) {
        // Gọi Service ở Bước 3 để lấy data tổng hợp
        List<DoctorWorkloadResponse> workloads = diagnosisSessionService.getDoctorWorkloads();
        model.addAttribute("workloads", workloads);
        // Trả về view HTML: templates/receptionist/doctor-workload.html (Bước 5)
        return "receptionist/doctor-workload";
    }

    // ==================== Màn hình 2: Chi tiết ca của bác sĩ ====================
    // [Nguyen The Hieu]: Bước 4 - Controller: Nhận request GET /receptionist/doctor-workload/{doctorId}
    @GetMapping("/doctor-workload/{doctorId}")
    public String doctorWorkloadDetail(
            @PathVariable Integer doctorId,
            @RequestParam(value = "dateFilter", defaultValue = "ALL") String dateFilter,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        // Tìm thông tin bác sĩ để hiển thị tên lên giao diện
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ"));
        model.addAttribute("doctorName", doctor.getFullName());
        model.addAttribute("doctorId", doctorId);

        // [Nguyen The Hieu]: Xử lý logic tính toán khoảng ngày (startDateTime, endDateTime) dựa trên filter được chọn trên giao diện
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        LocalDate today = LocalDate.now();

        switch (dateFilter) {
            case "TODAY":
                startDateTime = today.atStartOfDay();
                endDateTime = today.atTime(LocalTime.MAX);
                break;
            case "LAST_7_DAYS":
                startDateTime = today.minusDays(6).atStartOfDay();
                endDateTime = today.atTime(LocalTime.MAX);
                break;
            case "THIS_MONTH":
                startDateTime = today.withDayOfMonth(1).atStartOfDay();
                endDateTime = today.atTime(LocalTime.MAX);
                break;
            case "CUSTOM":
                if (startDate != null) {
                    startDateTime = startDate.atStartOfDay();
                }
                if (endDate != null) {
                    endDateTime = endDate.atTime(LocalTime.MAX);
                }
                break;
            default: // ALL - Mặc định không set limit thời gian
                break;
        }

        // Truyền xuống Service ở Bước 3 để lấy danh sách ca khám (đã lọc & phân trang)
        Page<DiagnosisSession> sessions = diagnosisSessionService.getSessionsByDoctor(
                doctorId, startDateTime, endDateTime, PageRequest.of(page, size));

        model.addAttribute("sessions", sessions);
        model.addAttribute("dateFilter", dateFilter);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sessions.getTotalPages());
        model.addAttribute("totalElements", sessions.getTotalElements());

        // Trả về view HTML: templates/receptionist/doctor-workload-detail.html (Bước 5)
        return "receptionist/doctor-workload-detail";
    }
}
