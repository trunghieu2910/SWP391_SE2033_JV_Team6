package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.entity.User;

public interface AccountLockoutService {

    public void registerFailedAttempt(String loginIdentifier);

    public void resetFailedAttempts(User user);
}
