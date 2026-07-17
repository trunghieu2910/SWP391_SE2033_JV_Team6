package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.*;

public interface AuthService {

    void logout();

    void register(RegisterRequest request);

    void verifyRegistrationOtp(OtpVerificationRequest request);

    void resendRegistrationOtp(ResendOtpRequest request);

    void registerByReceptionist(RegisterRequest request);
}
