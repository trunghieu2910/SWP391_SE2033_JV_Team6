package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.dto.response.UserRespone;
import com.mycompany.jpademo.backend.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserRespone> getAllUser() {
        return adminService.getAllUser();
    }

    @GetMapping("/users/search")
    public List<UserRespone> searchUsers(@RequestParam String keyword) {
        return adminService.searchUsers(keyword, keyword);
    }

    @PatchMapping("/users/ban")
    public ResponseEntity<String> banUser(@RequestBody BanUserRequest request) {
        return adminService.banUser(request);
    }

    @PatchMapping("/users/unban")
    public ResponseEntity<String> unbanUser(@RequestBody UnbanRequest request) {
        return adminService.unbanUser(request);
    }

    @GetMapping("/doctors/pending")
    public List<UserRespone> getPendingDoctors() {
        return adminService.getPendingDoctors();
    }

    @PatchMapping("/doctors/approve")
    public ResponseEntity<String> approveDoctor(@RequestBody ApproveDoctorRequest request) {
        return adminService.approveDoctor(request);
    }

    @PatchMapping("/doctors/reject")
    public ResponseEntity<String> rejectDoctor(@RequestBody RejectDoctorRequest request) {
        return adminService.rejectDoctor(request);
    }
}
