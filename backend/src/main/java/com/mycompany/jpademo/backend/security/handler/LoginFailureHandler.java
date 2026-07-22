package com.mycompany.jpademo.backend.security.handler;

import com.mycompany.jpademo.backend.exception.AccountTemporarilyLockedException;
import com.mycompany.jpademo.backend.service.interfaces.AccountLockoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Runs when a form login attempt FAILS (wrong password, account temporarily
 * locked, etc.). Distinguishes between the two failure types so the login
 * page can redirect with the correct error code and show the right message.
 */
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final AccountLockoutService accountLockoutService;

    /**
     * Invoked by Spring Security whenever the AuthenticationProvider throws
     * an exception.
     *   - If it's an AccountTemporarilyLockedException (account currently
     *     locked due to too many failed attempts) -> redirect with "?error=locked".
     *   - If it's a BadCredentialsException (wrong password) -> increment the
     *     failed-attempt counter via accountLockoutService.registerFailedAttempt(),
     *     then redirect with "?error=invalid".
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        Throwable actualCause = exception;
        if (exception instanceof InternalAuthenticationServiceException && exception.getCause() != null) {
            actualCause = exception.getCause();
        }

        if (actualCause instanceof AccountTemporarilyLockedException) {
            response.sendRedirect("/auth/login?error=locked");
            return;
        }

        if (exception instanceof BadCredentialsException) {
            String loginIdentifier = request.getParameter("login");
            if (loginIdentifier != null && !loginIdentifier.isBlank()) {
                accountLockoutService.registerFailedAttempt(loginIdentifier);
            }
        }

        response.sendRedirect("/auth/login?error=invalid");
    }
}