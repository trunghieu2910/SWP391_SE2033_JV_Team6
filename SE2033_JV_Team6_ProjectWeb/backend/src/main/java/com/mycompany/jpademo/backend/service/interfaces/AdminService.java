package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.UserRespone;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdminService {
    List<UserRespone> getAllUser();

    List<UserRespone> searchUsers(String keyword);

    ResponseEntity<String> banUser(Integer userId);

    ResponseEntity<String> unbanUser(Integer userId);

    List<UserRespone> getPendingDoctors();

    ResponseEntity<String> approveDoctor(Integer userId);

    ResponseEntity<String> rejectDoctor(Integer userId);
}
