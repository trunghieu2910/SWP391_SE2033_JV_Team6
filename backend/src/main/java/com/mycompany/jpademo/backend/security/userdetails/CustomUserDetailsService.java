package com.mycompany.jpademo.backend.security.userdetails;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.AccountTemporarilyLockedException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Bridge between Spring Security and the user database.
 * Whenever someone attempts to log in, Spring Security calls
 * loadUserByUsername() to fetch the account details before matching the password.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Looks up the user by the "username" that was entered — in practice
     * this accepts email, username, phone number, or national ID
     * (multi-method login).
     * If the account is currently under a temporary lock (lockedUntil is
     * still in the future), throws AccountTemporarilyLockedException right
     * here, BEFORE the password-matching step even runs (rejecting early so
     * the response time doesn't leak whether the failure was "wrong
     * password" vs. "account locked").
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmailOrUsernameOrPhoneNumberOrNationalId(username, username, username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountTemporarilyLockedException(user.getLockedUntil());
        }

        return new CustomUserDetails(user);
    }
}
