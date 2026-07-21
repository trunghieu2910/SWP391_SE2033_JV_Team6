package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.InitiateCreateDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingDoctorRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {
    Page<UserResponse> getUser(String keyword, String role, UserStatus status,
                               LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    boolean updateUserStatus(UpdateUserStatusRequest request, User admin);

    void verifyAndCreateDoctor(VerifyPendingDoctorRequest request, User admin);

    InitiateCreateDoctorResponse initiateCreateDoctor(InitiateCreateDoctorRequest request, User admin);

    DashboardPageResponse getDashboardPageData(LocalDate startDate, LocalDate endDate);

    UserDetailResponse getUserDetail(Integer userId);

    ChartStatsResponse getChartStats(LocalDate startDate, LocalDate endDate);

    GlobalSearchResponse searchGlobal(String keyword);

    Map<String, Object> resendOtp(String adminEmail);

    User getAdminUser();

    CertificateFileResponse getDoctorCertificate(Integer userId);

    List<String> getRoleName();

    List<String> getUserStatus();
}