package com.mycompany.jpademo.backend.util;

import com.mycompany.jpademo.backend.cache.OtpData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class OtpUtil {

    private static final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    /** How long a generated OTP stays valid before it must be re-requested. */
    private static final int OTP_EXPIRE_MINUTES = 2;

    /** Maximum number of wrong-code attempts allowed before an OTP is invalidated. */
    private static final int MAX_OTP_ATTEMPTS = 5;

    /** Removes all OTP entries whose expiry time has already passed. */
    public static void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        otpStorage.entrySet().removeIf(e -> now.isAfter(e.getValue().getExpireTime()));
    }

    /** Generates a random 6-digit numeric OTP code. */
    public static String generateOtp(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Stores (or overwrites) the active OTP for an email, valid for
     * {@link #OTP_EXPIRE_MINUTES} minutes from now.
     */
    public static void saveOtp(String email, String otp){
        OtpData otpData = OtpData.builder()
                .otp(otp)
                .expireTime(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES))
                .build();

        otpStorage.put(email, otpData);
    }

    /**
     * Checks whether {@code otp} matches the currently stored, non-expired
     * OTP for {@code email}. A wrong guess increments the failed-attempt
     * counter; once {@link #MAX_OTP_ATTEMPTS} is reached the OTP is
     * discarded entirely, forcing the user to request a new one.
     *
     * @return true only if the OTP exists, has not expired, has not
     *         exceeded the attempt limit, and matches
     */
    public static boolean verifyOtp(String email, String otp){
        OtpData otpData = otpStorage.get(email);

        if (otpData == null){
            return false;
        }

        if (LocalDateTime.now().isAfter(otpData.getExpireTime())){
            otpStorage.remove(email);
            return false;
        }

        // ── MỚI: nếu đã sai quá số lần cho phép, khóa OTP này luôn,
        //          buộc người dùng phải bấm "Gửi lại" để lấy mã mới ──
        if (otpData.getFailedAttempts() >= MAX_OTP_ATTEMPTS) {
            otpStorage.remove(email);
            return false;
        }

        boolean matched = otpData.getOtp().equals(otp);

        if (!matched) {
            otpData.setFailedAttempts(otpData.getFailedAttempts() + 1);
        }

        return matched;
    }

    /** Deletes any stored OTP for the given email (e.g. after successful use). */
    public static void  removeOtp(String email){
        otpStorage.remove(email);
    }

    /**
     * Returns how much time is left on the given email's active OTP.
     *
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
