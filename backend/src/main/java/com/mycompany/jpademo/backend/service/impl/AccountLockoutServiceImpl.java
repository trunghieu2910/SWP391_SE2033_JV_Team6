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

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Gọi khi đăng nhập sai mật khẩu (KHÔNG gọi khi tài khoản đã BANNED/đang bị khóa tạm,
     * vì lúc đó luồng đã bị chặn từ CustomUserDetailsService trước khi tới đây).
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

    @Override
    public void resetFailedAttempts(User user) {
        if ((user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0)
                || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

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
