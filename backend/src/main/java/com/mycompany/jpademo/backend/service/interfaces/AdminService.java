package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.CreateDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.response.DashboardStatsResponse;
import com.mycompany.jpademo.backend.dto.response.UserDetailResponse;
import com.mycompany.jpademo.backend.dto.response.UserResponse;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    Page<UserResponse> getUser(String keyword, String role, UserStatus status, Pageable pageable);

    ResponseEntity<String> updateUserStatus(UpdateUserStatusRequest request);

    ResponseEntity<String> createDoctor(CreateDoctorRequest request);

    DashboardStatsResponse getDashboardStats();

    UserDetailResponse getUserDetail(Integer userId);
}
