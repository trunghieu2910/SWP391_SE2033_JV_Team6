package com.mycompany.jpademo.backend.security.handler;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

/**
 * Runs after Spring Security has already invalidated the session (logout
 * succeeded). Responsibilities: update the user's last-logout timestamp in
 * the DB, write a SystemLog entry with action = "LOGOUT", then send the
 * user back to the login page.
 */
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final UserRepository userRepository;
    private final SystemLogService systemLogService;

    /**
     * @Transactional because there's a DB UPDATE (updateLastLogoutTime)
     * happening right inside the logout request handling — must be
     * committed before the response is sent.
     * If Authentication is null (a rare case, e.g. the session had already
     * expired before the logout button was clicked), the logging step is
     * simply skipped — no error is thrown.
     */
    @Override
    @Transactional
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Integer userId = userDetails.getUser().getUserId();
            userRepository.updateLastLogoutTime(userId, LocalDateTime.now());

            User user = userDetails.getUser();
            systemLogService.logActivity("Users", user.getUserId(), "LOGOUT",
                    "Người dùng đăng xuất khỏi hệ thống (" + user.getEmail() + ")");
        }
        response.sendRedirect("/auth/login?logout");
    }
}
