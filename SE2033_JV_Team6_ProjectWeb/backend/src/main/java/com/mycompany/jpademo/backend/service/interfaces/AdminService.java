package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.BanUserRequest;
import com.mycompany.jpademo.backend.dto.request.UnbanRequest;
import com.mycompany.jpademo.backend.dto.response.UserRespone;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdminService {
    List<UserRespone> getAllUser();

    List<UserRespone> searchUsers(String username, String email);

    ResponseEntity<String> banUser(BanUserRequest request);

    ResponseEntity<String> unbanUser(UnbanRequest request);

    List<UserRespone> getPendingDoctors();
}
