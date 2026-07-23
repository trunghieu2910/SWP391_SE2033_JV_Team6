package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;
import com.mycompany.jpademo.backend.entity.User;

public interface ProfileService {

    ProfileResponse getProfile(String username);

    ProfileResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, com.mycompany.jpademo.backend.dto.request.ChangePasswordRequest request);

    User getUserByLogin(String login);
}