package com.mycompany.jpademo.backend.util;

import java.util.regex.Pattern;

public class PasswordPolicyUtil {

    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

    public static boolean isValidPassword(String password){
        return Pattern.matches(PASSWORD_REGEX, password);
    }
}
