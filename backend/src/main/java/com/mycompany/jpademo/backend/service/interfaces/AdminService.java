package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.InitiateCreateDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingDoctorRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    Page<UserResponse> getUser(String keyword, String role, UserStatus status, Pageable pageable);

    ResponseEntity<String> updateUserStatus(UpdateUserStatusRequest request);

    DashboardStatsResponse getDashboardStats(java.time.LocalDate startDate, java.time.LocalDate endDate);

    UserDetailResponse getUserDetail(Integer userId);

    InitiateCreateDoctorResponse initiateCreateDoctor(InitiateCreateDoctorRequest request, User admin);

    ResponseEntity<String> verifyAndCreateDoctor(VerifyPendingDoctorRequest request, User admin);

    ChartStatsResponse getChartStats(java.time.LocalDate startDate, java.time.LocalDate endDate);

    GlobalSearchResponse searchGlobal(String keyword);
}

