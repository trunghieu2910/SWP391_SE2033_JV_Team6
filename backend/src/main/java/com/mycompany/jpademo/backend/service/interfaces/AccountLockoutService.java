package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.entity.User;

/**
 * Temporary account-locking mechanism triggered by repeated failed login
 * attempts — a brute-force protection measure. Called from
 * LoginFailureHandler (on failure) and RoleBasedSuccessHandler (on success,
 * to reset the counter).
 */
public interface AccountLockoutService {

    /**
     * Call when incorrect password is entered (DO NOT call when the account is BANNED/temporarily locked,
     * because the thread would have been blocked from CustomUserDetailsService before reaching this point).
     */
    public void registerFailedAttempt(String loginIdentifier);

    /** Only writes to the DB if there's actually something to reset (avoids
     *  an unnecessary UPDATE on every single successful login). */
    public void resetFailedAttempts(User user);
}
