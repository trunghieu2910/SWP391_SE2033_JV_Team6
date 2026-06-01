package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.OtpVerificationRequest;
import com.mycompany.jpademo.backend.dto.request.RegisterRequest;

public interface UserService {

    void register(RegisterRequest request);

    void verifyOtp(OtpVerificationRequest request);

    void resendOtp(String userName);
}
