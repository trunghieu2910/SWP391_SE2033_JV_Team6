package com.mycompany.jpademo.backend.aop.aspect;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.aop.interfaces.LoggableTarget;
import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.request.UnblockIpRequest;
import com.mycompany.jpademo.backend.dto.request.UpdateUserStatusRequest;
import com.mycompany.jpademo.backend.dto.request.VerifyPendingDoctorRequest;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
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
    public void logAdminAction(JoinPoint joinPoint,
                               AdminActionLog adminActionLog) {
        Object[] args = joinPoint.getArgs();
        Integer targetId = null;
        if (args.length > 0 && args[0] instanceof LoggableTarget target) {
            targetId = target.getTargetId();
        }
        String action = adminActionLog.action();
        String description = "";
        for (Object arg : args) {
            if (arg instanceof UpdateUserStatusRequest req) {
                if (req.getStatus() == UserStatus.BANNED) {
                    action = "BAN_USER";
                    description = "ADMIN: Khóa người dùng. Lý do: " + req.getReason();
                } else if (req.getStatus() == UserStatus.ACTIVE) {
                    action = "UNBAN_USER";
                    description = "ADMIN: Mở khóa/Kích hoạt người dùng. Lý do: " + req.getReason();
                } else if (req.getStatus() == UserStatus.INACTIVE) {
                    action = "SET_INACTIVE";
                    description = "ADMIN: Chuyển sang không hoạt động. Lý do: " + req.getReason();
                }
            }
            if (arg instanceof VerifyPendingDoctorRequest request) {
                description = "ADMIN: Tạo tài khoản bác sĩ";
            }
            if (arg instanceof BlockIpRequest request) {
                description = "ADMIN: Chặn IP " + request.getIpAddress() + ". Lý do: " + request.getReason();
            }
            if (arg instanceof UnblockIpRequest request) {
                description = "ADMIN: Mở chặn IP " + request.getIpAddress();
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.getPrincipal()
                instanceof CustomUserDetails customUserDetails) {
            currentUser = customUserDetails.getUser();
        }
        SystemLog systemLog = SystemLog.builder()
                .user(currentUser)
                .action(action)
                .targetType(adminActionLog.targetType())
                .targetId(targetId)
                .description(description)
                .build();
        systemLogRepository.save(systemLog);
    }

    @AfterThrowing(pointcut = "@annotation(adminActionLog)", throwing = "exception")
    public void logAdminActionFailure(JoinPoint joinPoint,
                                      AdminActionLog adminActionLog,
                                      Throwable exception) {
        Object[] args = joinPoint.getArgs();
        Integer targetId = null;
        if (args.length > 0 && args[0] instanceof LoggableTarget target) {
            targetId = target.getTargetId();
        }

        String action = "FAILED_" + adminActionLog.action();
        String description = "Thao tác thất bại. Lý do: " + exception.getMessage();
        for (Object arg : args) {
            if (arg instanceof UpdateUserStatusRequest req) {
                description = "ADMIN: Đổi trạng thái user THẤT BẠI. Trạng thái yêu cầu: "
                        + req.getStatus() + ". Lý do: " + exception.getMessage();
            }
            if (arg instanceof VerifyPendingDoctorRequest request) {
                description = "ADMIN: Tạo tài khoản bác sĩ THẤT BẠI. Lý do: " + exception.getMessage();
            }
            if (arg instanceof BlockIpRequest request) {
                description = "ADMIN: Chặn IP " + request.getIpAddress() + " THẤT BẠI. Lý do: " + exception.getMessage();
            }
            if (arg instanceof UnblockIpRequest request) {
                description = "ADMIN: Mở chặn IP " + request.getIpAddress() + " THẤT BẠI. Lý do: " + exception.getMessage();
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.getPrincipal()
                instanceof CustomUserDetails customUserDetails) {
            currentUser = customUserDetails.getUser();
        }
        SystemLog systemLog = SystemLog.builder()
                .user(currentUser)
                .action(action)
                .targetType(adminActionLog.targetType())
                .targetId(targetId)
                .description(description)
                .build();

        systemLogRepository.save(systemLog);
    }
}