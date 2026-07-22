package com.mycompany.jpademo.backend.security.handler;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.AccountLockoutService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Runs immediately AFTER a successful form login (email/username + password).
 * Responsibilities: log the login event, reset the failed-login-attempt
 * counter (anti-lockout), and redirect the user to the home page that
 * matches their role.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    private final SystemLogService systemLogService;
    private final AccountLockoutService accountLockoutService;

    private static final String DEFAULT_HOME = "/patient/home";

    private static final Map<String, String> ROLE_HOME = Map.of(
            "ROLE_ADMIN",    "/admin/dashboard",
            "ROLE_DOCTOR",   "/doctor/sessions",
            "ROLE_RECEPTIONIST", "/receptionist/create-session",
            "ROLE_PHARMACIST", "/pharmacist/",
            "ROLE_PATIENT",  "/patient/home",
            "ROLE_AITRAINER", "/patient/home",
            "ROLE_TECHNICAL", "/technical/dashboard"
    );

    /**
     * Automatically invoked by Spring Security right after the
     * AuthenticationProvider has successfully authenticated the user.
     * Steps:
     *   1. Extract the actual User from the Authentication (via CustomUserDetails).
     *   2. Reset the failed-login counter (accountLockoutService.resetFailedAttempts).
     *   3. Write a SystemLog entry with action = "LOGIN".
     *   4. Look up the ROLE_HOME map to find this role's home page and redirect there.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String target = DEFAULT_HOME;

        try {
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                User user = userDetails.getUser();

                accountLockoutService.resetFailedAttempts(user);

                systemLogService.logActivity("Users", user.getUserId(), "LOGIN",
                        "Đăng nhập thành công (" + user.getUserName() + ")");

                GrantedAuthority authority = authentication.getAuthorities().stream().findFirst().orElse(null);
                if (authority != null) {
                    String role = authority.getAuthority();
                    target = ROLE_HOME.getOrDefault(role, DEFAULT_HOME);
                }
            }
        } catch (Exception e) {
            log.error("Error in RoleBasedSuccessHandler: ", e);
        }

        response.sendRedirect(target);
    }
}
