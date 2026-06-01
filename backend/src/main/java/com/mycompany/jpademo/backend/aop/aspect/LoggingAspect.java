package com.mycompany.jpademo.backend.aop.aspect;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final SystemLogService systemLogService;

    // Lệnh này nghĩa là: Kích hoạt NGAY SAU KHI một hàm có gắn nhãn @LogActivity chạy xong thành công (Không bị lỗi Exception)
    @AfterReturning(pointcut = "@annotation(logAnnotation)")
    public void logAfterExecution(JoinPoint joinPoint, LogActivity logAnnotation) {

        // 1. Lấy thông tin từ Nhãn dán
        String action = logAnnotation.action();
        String targetType = logAnnotation.targetType();

        // 2. Tự động sinh mô tả nếu không truyền vào (Lấy tên hàm đang chạy)
        String description = logAnnotation.description();
        if (description.isEmpty()) {
            description = "Thực thi thành công hàm: " + joinPoint.getSignature().getName();
        }

        // 3. Ghi log âm thầm (Không cần quan tâm targetID ở mức độ cơ bản này)
        systemLogService.logActivity(
                targetType.isEmpty() ? null : targetType,
                null, // targetID để null vì AOP lấy ID động khá phức tạp
                action,
                description
        );
    }
}
