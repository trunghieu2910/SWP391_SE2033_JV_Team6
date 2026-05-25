package com.mycompany.jpademo.backend.controller;

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

    @PatchMapping("/users/ban/{userId}")
    public ResponseEntity<String> banUser(@PathVariable Integer userId) {
        return adminService.banUser(userId);
    }

    @PatchMapping("/users/unban/{userId}")
    public ResponseEntity<String> unbanUser(@PathVariable Integer userId) {
        return adminService.unbanUser(userId);
    }

    @GetMapping("/doctors/pending")
    public List<UserRespone> getPendingDoctors() {
        return adminService.getPendingDoctors();
    }

    @PatchMapping("/doctors/approve/{userId}")
    public ResponseEntity<String> approveDoctor(@PathVariable Integer userId) {
        return adminService.approveDoctor(userId);
    }

    @PatchMapping("/doctors/reject/{userId}")
    public ResponseEntity<String> rejectDoctor(@PathVariable Integer userId) {
        return adminService.rejectDoctor(userId);
    }
}
