package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.request.ApproveDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.BanUserRequest;
import com.mycompany.jpademo.backend.dto.request.RejectDoctorRequest;
import com.mycompany.jpademo.backend.dto.request.UnbanRequest;
import com.mycompany.jpademo.backend.dto.response.UserRespone;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.DoctorApprovalException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    private final EmailService emailService;

    @Override
    public List<UserRespone> getAllUser() {
        List<User> users = userRepository.findAll();
        return getUserRespones(users);
    }

    @Override
    public List<UserRespone> searchUsers(String username, String email) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(username, email);
        return getUserRespones(users);
    }

    @Override
    @AdminActionLog(action = "BAN_USER",
                    targetType = "User")
    public ResponseEntity<String> banUser(BanUserRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        user.setStatus(UserStatus.BLOCKED);
        emailService.sendEmail(user.getEmail(), "Tài khoản của bạn đã bị khoá", EmailUtil.buildBanAccountTemplate(user.getFullName()));
        userRepository.save(user);
        return ResponseEntity.ok("User banned successfully");
    }

    @Override
    @AdminActionLog(action = "UNBAN_USER",
                    targetType = "User")
    public ResponseEntity<String> unbanUser(UnbanRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        user.setStatus(UserStatus.ACTIVE);
        emailService.sendEmail(user.getEmail(), "Tài khoản của bạn đã được mở khoá", EmailUtil.buildUnbanAccountTemplate(user.getFullName()));
        userRepository.save(user);
        return ResponseEntity.ok("User unbanned successfully");
    }

    @Override
    public List<UserRespone> getPendingDoctors() {
        List<User> users = userRepository.findByRoleRoleNameAndStatus("DOCTOR", UserStatus.PENDING);
        return getUserRespones(users);
    }

    @Override
    @AdminActionLog(action = "APPROVE_DOCTOR",
                    targetType = "User")
    public ResponseEntity<String> approveDoctor(ApproveDoctorRequest request) {
        User user = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getDoctorId()));
        if (!user.getRole().getRoleName().equals("DOCTOR")) {
            throw new DoctorApprovalException("User is not a doctor");
        }
        user.setStatus(UserStatus.ACTIVE);
        emailService.sendEmail(user.getEmail(), "Tài khoản bác sĩ của bạn đã được phê duyệt", EmailUtil.buildDoctorApprovedTemplate(user.getFullName()));
        userRepository.save(user);
        return ResponseEntity.ok("Doctor approved successfully");
    }

    @Override
    @AdminActionLog(action = "REJECT_DOCTOR",
                    targetType = "User")
    public ResponseEntity<String> rejectDoctor(RejectDoctorRequest request) {
        User user = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getDoctorId()));
        if (!user.getRole().getRoleName().equals("DOCTOR")) {
            throw new DoctorApprovalException("User is not a doctor");
        }
        user.setStatus(UserStatus.REJECTED);
        emailService.sendEmail(user.getEmail(), "Tài khoản bác sĩ của bạn đã bị từ chối", EmailUtil.buildDoctorRejectedTemplate(user.getFullName()));
        userRepository.save(user);
        return ResponseEntity.ok("Doctor rejected successfully");
    }

    @NonNull
    private List<UserRespone> getUserRespones(List<User> users) {
        List<UserRespone> respones = new ArrayList<>();
        for (User user: users) {
            UserRespone respone = UserRespone.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .roleName(user.getRole() != null ? user.getRole().getRoleName() :"N/A")
                    .status(user.getStatus())
                    .certificate(user.getCertificate() != null ? user.getCertificate() :"N/A")
                    .createdAt(user.getCreatedAt())
                    .build();
            respones.add(respone);
        }
        return respones;
    }
}
