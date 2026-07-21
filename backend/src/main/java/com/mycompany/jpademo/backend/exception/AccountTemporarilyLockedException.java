package com.mycompany.jpademo.backend.exception;

import org.springframework.security.authentication.LockedException;
import java.time.LocalDateTime;

public class AccountTemporarilyLockedException extends LockedException {
    private final LocalDateTime lockedUntil;

    public AccountTemporarilyLockedException(LocalDateTime lockedUntil) {
        super("Tài khoản đang bị khóa tạm thời đến " + lockedUntil);
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}