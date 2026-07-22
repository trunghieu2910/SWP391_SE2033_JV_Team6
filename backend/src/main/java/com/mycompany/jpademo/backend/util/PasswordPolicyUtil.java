package com.mycompany.jpademo.backend.util;

import java.util.regex.Pattern;

/** Centralized password-strength rule, shared by registration and reset-password flows. */

public class PasswordPolicyUtil {

    /** Requires: at least 1 uppercase, 1 lowercase, 1 digit, 1 special
     *  character from {@code @$!%*?&}, and a minimum length of 8. */
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

    /** Checks whether {@code password} satisfies {@link #PASSWORD_REGEX}. */
    public static boolean isValidPassword(String password){
        return Pattern.matches(PASSWORD_REGEX, password);
    }
}
