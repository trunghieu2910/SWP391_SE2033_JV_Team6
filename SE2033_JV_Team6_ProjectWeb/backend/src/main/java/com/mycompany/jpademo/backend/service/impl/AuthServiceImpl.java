package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.LoginRequest;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.factory.AuthFactory;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.LoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthFactory authFactory;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Verify login/password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getLogin(),
                request.getPassword())
        );

        // Query the user again to retrieve the full information.
        User user = userRepository.findByEmailOrUsernameOrPhoneNumber(
                request.getLogin(),
                request.getLogin(),
                request.getLogin()
        ).orElseThrow(() -> new UserNotFoundException("Thông tin đăng nhập không hợp lệ"));

        // Choose a strategy based on your role.
        LoginStrategy strategy = authFactory.getLoginStrategy(user.getRole().getRoleName());

        return strategy.login(user);
    }
}
