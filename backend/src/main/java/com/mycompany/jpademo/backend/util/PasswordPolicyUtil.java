package com.mycompany.jpademo.backend.util;

import java.util.UUID;
import java.util.regex.Pattern;

public class PasswordPolicyUtil {

    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

    public static boolean isValidPassword(String password){
        return Pattern.matches(PASSWORD_REGEX, password);
    }

    public static String generateRandomPassword() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // Kết hợp 8 ký tự ngẫu nhiên từ UUID + 4 ký tự cứng cứng chuẩn quy tắc
        return uuid.substring(0, 8) + "X@1a";
    }
}
