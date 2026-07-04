package com.mycompany.jpademo.backend.util;

import com.mycompany.jpademo.backend.cache.OtpData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class OtpUtil {

    private static final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRE_MINUTES = 2;

    public static String generateOtp(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public static void saveOtp(String email, String otp){
        OtpData otpData = OtpData.builder()
                .otp(otp)
                .expireTime(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES))
                .build();

        otpStorage.put(email, otpData);
    }

    public static boolean verifyOtp(String email, String otp){
        OtpData otpData = otpStorage.get(email);

        if (otpData == null){
            return false;
        }

        if (LocalDateTime.now().isAfter(otpData.getExpireTime())){
            otpStorage.remove(email);
            return false;
        }

        return otpData.getOtp().equals(otp);
    }

    public static void  removeOtp(String email){
        otpStorage.remove(email);
    }

    /**
     * Lấy thời gian còn lại của OTP (tính bằng giây)
     * @param email Email cần kiểm tra
     * @return Số giây còn lại, 0 nếu OTP không tồn tại hoặc đã hết hạn
     */
    public static int getRemainingTime(String email) {
        OtpData data = otpStorage.get(email);
        if (data == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = data.getExpireTime();

        if (now.isAfter(expireTime)) {
            otpStorage.remove(email);
            return 0;
        }

        long seconds = java.time.Duration.between(now, expireTime).getSeconds();
        return (int) Math.max(seconds, 0);
    }
}
