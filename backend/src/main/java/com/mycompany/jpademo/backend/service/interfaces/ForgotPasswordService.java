package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.ForgotPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.ResetPasswordRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyOtpRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.VerifyOtpResponse;
import org.springframework.http.ResponseEntity;

/**
 * Business logic for the "forgot password" self-service flow: request an
 * OTP, verify it, then set a new password.
 */
public interface ForgotPasswordService {

    /**
     * Generates and emails a one-time password (OTP) for the given email,
     * if an account exists for it.
     * <p>
     * Always returns a generic "OTP sent" response and never reveals
     * whether the email actually exists in the system, to prevent
     * user-enumeration via this endpoint.
     */
    ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordRequest request);

    /**
     * Verifies the OTP submitted for the given email and, if valid, issues
     * a short-lived reset token that authorizes exactly one password change.
     *
     * @throws com.mycompany.jpademo.backend.exception.InvalidOtpException if
     *         the OTP is missing, expired, exhausted, or does not match
     */
    ResponseEntity<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request);

    /**
     * Sets a new password for the account identified by a valid reset token.
     *
     * @throws com.mycompany.jpademo.backend.exception.InvalidResetTokenException
     *         if the token is invalid or expired
     * @throws com.mycompany.jpademo.backend.exception.WeakPasswordException
     *         if the new password does not satisfy {@link com.mycompany.jpademo.backend.util.PasswordPolicyUtil}
     * @throws com.mycompany.jpademo.backend.exception.UseResetTokenAgainException
     *         if this token was already used to change the password once
     * @throws com.mycompany.jpademo.backend.exception.DuplicatePasswordException
     *         if the new password is the same as the current one
     */
    ResponseEntity<ApiResponse> resetPassword(ResetPasswordRequest request);
}
