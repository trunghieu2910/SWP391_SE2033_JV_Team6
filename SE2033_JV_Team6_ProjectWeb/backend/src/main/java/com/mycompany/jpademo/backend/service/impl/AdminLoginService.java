package com.mycompany.jpademo.backend.service.impls;

import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.security.jwt.JwtService;
import com.mycompany.jpademo.backend.service.interfaces.LoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLoginService implements LoginStrategy {

    private final JwtService jwtService;

    @Override
    public LoginResponse login(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().getRoleName())
                .build();

        String token = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getRoleName())
                .build();
    }
}
