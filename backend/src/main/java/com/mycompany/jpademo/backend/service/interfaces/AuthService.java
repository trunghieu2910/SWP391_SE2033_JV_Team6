package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.LoginRequest;
import com.mycompany.jpademo.backend.dto.request.OtpVerificationRequest;
import com.mycompany.jpademo.backend.dto.request.RegisterRequest;
import com.mycompany.jpademo.backend.dto.request.ResendOtpRequest;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout();

    void register(RegisterRequest request);

    void verifyRegistrationOtp(OtpVerificationRequest request);

    void resendRegistrationOtp(ResendOtpRequest request);
}
