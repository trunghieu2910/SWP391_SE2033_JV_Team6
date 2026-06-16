package com.mycompany.jpademo.backend.aop.aspect;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final SystemLogService systemLogService;

    // Lệnh này nghĩa là: Kích hoạt NGAY SAU KHI một hàm có gắn nhãn @LogActivity chạy xong thành công (Không bị lỗi Exception)
    @Around("@annotation(logAnnotation)")
    public Object logAroundExecution(ProceedingJoinPoint joinPoint, LogActivity logAnnotation) throws Throwable {

        // ==========================================
        // GIAI ĐOẠN 1: TRƯỚC KHI HÀM CHẠY
        // ==========================================
        // Dành cho Logout hoặc các API bình thường: Tranh thủ lấy ID trước khi nó bị xóa
        Integer preExecutionUserId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            preExecutionUserId = ((CustomUserDetails) auth.getPrincipal()).getUser().getUserId();
        }

        // ==========================================
        // GIAI ĐOẠN 2: CHO PHÉP HÀM CHẠY
        // ==========================================
        Object result = joinPoint.proceed(); // Hàm thực sự (login, logout, v.v.) sẽ chạy ở đây

        // ==========================================
        // GIAI ĐOẠN 3: SAU KHI HÀM CHẠY XONG
        // ==========================================
        try {
            String action = logAnnotation.action();
            String targetType = logAnnotation.targetType();
            String description = logAnnotation.description();
            if (description.isEmpty()) {
                description = "Thực thi thành công hàm: " + joinPoint.getSignature().getName();
            }

            Integer targetId = preExecutionUserId;

            // Dành riêng cho Đăng nhập/Đăng ký: Nếu lúc trước chưa có ID, thì giờ lấy từ kết quả hàm trả về
            if (result instanceof LoginResponse loginResponse) {
                targetId = loginResponse.getUserId();
            } else if ("GOOGLE_LOGIN".equals(action)) {
                // Nếu là luồng Google Login nhưng chưa có tài khoản (Không trả về LoginResponse)
                // Tự động đổi tên action và description cho hợp lý hơn
                action = "GOOGLE_INIT_AUTH";
                description = "Xác thực Google lần đầu (Chuyển hướng đăng ký)";
            }

            // Tiến hành ghi log
            systemLogService.logActivity(
                    targetType.isEmpty() ? null : targetType,
                    targetId, // Lúc này chắc chắn targetId đã có số (hoặc null nếu là hệ thống)
                    action,
                    description
            );
        } catch (Exception e) {
            System.err.println("====== [AOP] LỖI KHI GHI LOG: " + e.getMessage() + " ======");
        }

        // Bắt buộc phải return lại kết quả để Controller còn phản hồi cho Frontend
        return result;
    }
}
