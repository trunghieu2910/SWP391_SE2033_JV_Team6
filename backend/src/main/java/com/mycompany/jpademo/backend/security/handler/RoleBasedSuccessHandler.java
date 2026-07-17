package com.mycompany.jpademo.backend.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    private static final String DEFAULT_HOME = "/patient/home";

    private static final Map<String, String> ROLE_HOME = Map.of(
            "ROLE_ADMIN",    "/admin/dashboard",
            "ROLE_DOCTOR",   "/doctor/sessions",
            "ROLE_RECEPTIONIST", "/receptionist/create-session",
            "ROLE_PATIENT",  "/patient/home",
            "ROLE_AITRAINER", "/patient/home"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String target = DEFAULT_HOME;

        try {
            if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                GrantedAuthority authority = authentication.getAuthorities().stream().findFirst().orElse(null);
                if (authority != null) {
                    String role = authority.getAuthority();
                    target = ROLE_HOME.getOrDefault(role, DEFAULT_HOME);
                    log.info("Login successful for user: {}, role: {}, redirecting to: {}",
                            authentication.getName(), role, target);
                } else {
                    log.warn("No authorities found for user: {}", authentication.getName());
                }
            } else {
                log.warn("Authentication or authorities is null/empty for user: {}",
                        authentication != null ? authentication.getName() : "unknown");
            }
        } catch (Exception e) {
            log.error("Error in RoleBasedSuccessHandler: ", e);
        }

        response.sendRedirect(target);
    }
}
