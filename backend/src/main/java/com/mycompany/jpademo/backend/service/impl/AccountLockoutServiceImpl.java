package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.AccountLockoutService;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockoutServiceImpl implements AccountLockoutService {

    // Lockout threshold: 5 consecutive failures -> locked for 15 minutes.
    // Changing these two constants is the only place needed to tune how
    // strict the brute-force protection is.
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Call when incorrect password is entered (DO NOT call when the account is BANNED/temporarily locked,
     * because the thread would have been blocked from CustomUserDetailsService before reaching this point).
     */
    @Override
    public void registerFailedAttempt(String loginIdentifier) {
        Optional<User> optUser = userRepository.findByEmailOrUsernameOrPhoneNumberOrNationalId(
                loginIdentifier, loginIdentifier, loginIdentifier, loginIdentifier);

        if (optUser.isEmpty()) {
            return; // không tiết lộ gì thêm nếu tài khoản không tồn tại
        }

        User user = optUser.get();
        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            user.setLockedUntil(lockUntil);
            user.setFailedLoginAttempts(0); // reset để lần khóa tiếp theo tính lại từ đầu
            userRepository.save(user);

            sendLockoutWarningEmail(user);
            log.warn("Account {} locked until {} due to {} failed login attempts",
                    user.getUserName(), lockUntil, attempts);
        } else {
            user.setFailedLoginAttempts(attempts);
            userRepository.save(user);
        }
    }

    /** Only writes to the DB if there's actually something to reset (avoids
     *  an unnecessary UPDATE on every single successful login). */
    @Override
    public void resetFailedAttempts(User user) {
        if ((user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0)
                || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    /** Sends a warning email when an account has just been locked — any
     *  email failure (SMTP down, etc.) is only logged, and must NEVER throw
     *  and break the main login flow. */
    private void sendLockoutWarningEmail(User user) {
        if (user.getEmail() == null) return;
        try {
            String subject = "Cảnh báo bảo mật: Tài khoản bị khóa tạm thời";
            String body = EmailUtil.buildAccountLockedTemplate(user.getFullName(), LOCK_DURATION_MINUTES);
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            log.error("Failed to send lockout warning email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
