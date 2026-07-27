package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.InitiateCreateStaffRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingStaffRequest;
import com.mycompany.jpademo.backend.dto.response.*;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Author: GiangLTHE194888
 * Task: Service interface defining operations for system administration, including user management, dashboard statistics, and doctor account initialization.
 */
public interface AdminService {
    /** Retrieves a paginated list of users based on search filters. */
    Page<UserResponse> getUser(String keyword, String role, UserStatus status,
                               LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /** Updates the status of a specific user. */
    boolean updateUserStatus(UpdateUserStatusRequest request, User admin);

    void verifyAndCreateStaff(VerifyPendingStaffRequest request, User admin);

    InitiateCreateStaffResponse initiateCreateStaff(InitiateCreateStaffRequest request, User admin);

    /** Retrieves page data for the admin dashboard. */
    DashboardPageResponse getDashboardPageData(LocalDate startDate, LocalDate endDate);

    /** Retrieves details of a specific user. */
    UserDetailResponse getUserDetail(Integer userId);

    /** Retrieves chart statistical data for a given date range. */
    ChartStatsResponse getChartStats(LocalDate startDate, LocalDate endDate);

    /** Performs a global search across different entities. */
    GlobalSearchResponse searchGlobal(String keyword);

    /** Resends the verification OTP code to the admin email. */
    Map<String, Object> resendOtp(String adminEmail);

    /** Retrieves a staff's certificate resource and metadata. */
    CertificateFileResponse getStaffCertificate(Integer userId);

    /** Retrieves all available role names. */
    List<String> getRoleName();

    /** Retrieves all available user status names. */
    List<String> getUserStatus();
}