package com.mycompany.jpademo.backend.aop.aspect;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminActionLogAspect {
    private final SystemLogRepository systemLogRepository;

    @AfterReturning(value = "@annotation(adminActionLog)")
    public void logAdminAction(JoinPoint joinPoint, AdminActionLog adminActionLog) {
        Object[] args = joinPoint.getArgs();
        Integer targetID = null;
        if (args.length > 0 && args[0] instanceof Integer) {
            targetID = Math.toIntExact((Integer) args[0]);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            currentUser = customUserDetails.getUser();
        }
        SystemLog systemLog = SystemLog.builder()
                .user(currentUser)
                .action(adminActionLog.action())
                .targetType(adminActionLog.targetType())
                .targetId(targetID)
                .description("Admin executed action: " + adminActionLog.action())
                .build();
        systemLogRepository.save(systemLog);
    }
}