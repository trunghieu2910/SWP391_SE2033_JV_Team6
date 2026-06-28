package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.UpdateProfileRequest;
import com.mycompany.jpademo.backend.dto.response.ProfileResponse;

public interface ProfileService {

    ProfileResponse getProfile(String username);

    ProfileResponse updateProfile(String username, UpdateProfileRequest request);
}