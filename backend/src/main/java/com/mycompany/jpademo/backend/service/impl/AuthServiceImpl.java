package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.request.LoginRequest;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.factory.AuthFactory;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.LoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthFactory authFactory;

    @Override
    @LogActivity(action = "LOGIN", targetType = "Users", description = "Người dùng đăng nhập")
    public LoginResponse login(LoginRequest request) {
        // Verify login/password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getLogin(),
                request.getPassword())
        );

        // Query the user again to retrieve the full information.
        User user = userRepository.findByEmailOrUsernameOrPhoneNumberOrNationalId(
                request.getLogin(),
                request.getLogin(),
                request.getLogin(),
                request.getLogin()
        ).orElseThrow(() -> new UserNotFoundException("Thông tin đăng nhập không hợp lệ"));

        // Choose a strategy based on your role.
        LoginStrategy strategy = authFactory.getLoginStrategy(user.getRole().getRoleName());

        return strategy.login(user);
    }

    @Override
    @LogActivity(action = "LOGOUT", targetType = "Users", description = "Người dùng đăng xuất")
    public void logout() {
        // 1. Lấy thông tin người dùng đang đăng nhập hiện tại từ bảng vàng SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userDetails.getUser();

            // 2. Cập nhật thời điểm đăng xuất là NGAY BÂY GIỜ
            currentUser.setLastLogoutTime(LocalDateTime.now());
            userRepository.save(currentUser);
        }

        // 3. Xóa sạch thông tin đăng nhập trên RAM của phiên làm việc này
        SecurityContextHolder.clearContext();
    }
}
