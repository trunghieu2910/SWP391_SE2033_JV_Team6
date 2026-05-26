package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyOtpRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import org.springframework.http.ResponseEntity;

public interface ForgotPasswordService {

    ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordRequest request);

    ResponseEntity<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request);

    ResponseEntity<ApiResponse> resetPassword(ResetPasswordRequest request);
}
