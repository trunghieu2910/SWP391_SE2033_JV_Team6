package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout();

    void register(RegisterRequest request);

    void verifyRegistrationOtp(OtpVerificationRequest request);

    void resendRegistrationOtp(ResendOtpRequest request);

    Object handleGoogleLogin(String idToken);

    LoginResponse completeGoogleRegistration(GoogleCompleteRequest request);
}
