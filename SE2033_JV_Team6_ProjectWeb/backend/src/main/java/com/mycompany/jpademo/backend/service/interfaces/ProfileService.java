package com.mycompany.jpademo.backend.service.interfaces;
import com.mycompany.jpademo.backend.dto.request.ChangePasswordRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.MessageResponse;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;
public interface ProfileService {
    ProfileResponse getProfile(String email);

    ProfileResponse updateProfile(String email, UpdateProfileRequest request);

    MessageResponse changePassword(String email, ChangePasswordRequest request);
}
