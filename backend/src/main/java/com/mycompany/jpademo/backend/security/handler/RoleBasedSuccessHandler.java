package com.mycompany.jpademo.backend.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    private static final Map<String, String> ROLE_HOME = Map.of(
            "ROLE_DOCTOR",  "/doctor/dashboard",
            "ROLE_PATIENT", "/patient/home",
            "ROLE_ADMIN",   "/admin/dashboard"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String target = ROLE_HOME.getOrDefault(role, "/");
        response.sendRedirect(target);
    }
}
