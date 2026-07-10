package com.mycompany.jpademo.backend.security.handler;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Integer userId = userDetails.getUser().getUserId();
            userRepository.updateLastLogoutTime(userId, LocalDateTime.now());
        }
        response.sendRedirect("/auth/login?logout");
    }
}
