package com.mycompany.jpademo.backend.security.userdetails;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String login) {
        User user = userRepository.findByEmailOrUsernameOrPhoneNumber(login, login, login)
                .orElseThrow(() -> new UserNotFoundException("Thông tin đăng nhập không hợp lệ"));

        return CustomUserDetails.builder()
                .userID(user.getUserID())
                .username(user.getUsername())
                .passwordHash(user.getPasswordHash())
                .status(user.getStatus().toString())
                .roleName(user.getRole().getRoleName())
                .build();
    }

}
