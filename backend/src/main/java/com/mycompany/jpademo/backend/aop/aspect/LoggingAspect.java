package com.mycompany.jpademo.backend.aop.aspect;

import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.aop.context.AuditLogContext;
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

        String action = logAnnotation.action();
        String targetType = logAnnotation.targetType();
        String description = logAnnotation.description();

        Integer targetId = AuditLogContext.getTargetId();

        systemLogService.logActivity(
                targetType.isEmpty() ? null : targetType,
                targetId,
                action,
                description
        );
    }
}
