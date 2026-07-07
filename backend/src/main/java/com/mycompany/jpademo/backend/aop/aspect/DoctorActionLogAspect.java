package com.mycompany.jpademo.backend.aop.aspect;

import com.mycompany.jpademo.backend.aop.annotation.DoctorActionLog;
import com.mycompany.jpademo.backend.entity.SystemLog;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.repository.SystemLogRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DoctorActionLogAspect {
    private final SystemLogRepository systemLogRepository;

    @AfterReturning(value = "@annotation(doctorActionLog)")
    public void logDoctorAction(JoinPoint joinPoint,
                                DoctorActionLog doctorActionLog) {
        try {
            Object[] args = joinPoint.getArgs();
            Integer targetId = null;
            String description = "";
            String action = doctorActionLog.action();
            String targetType = doctorActionLog.targetType();

            Integer sessionId = null;
            Integer doctorId = null;

            for (Object arg : args) {
                if (arg instanceof Integer) {
                    if (sessionId == null) {
                        sessionId = (Integer) arg;
                    } else {
                        doctorId = (Integer) arg;
                    }
                }
            }

            switch (action) {
                case "UPDATE_SESSION_STATUS":
                    for (Object arg : args) {
                        if (arg instanceof com.mycompany.jpademo.backend.dto.request.UpdateSessionStatusRequest req) {
                            targetId = req.getSessionId();
                            String status = req.getStatus() != null ? req.getStatus().name() : "UNKNOWN";
                            String statusVietnamese = getStatusVietnamese(req.getStatus());
                            description = "Bác sĩ đã cập nhật trạng thái ca chẩn đoán #" + targetId + " thành: " + statusVietnamese;
                            break;
                        }
                    }
                    break;

                case "UPDATE_SESSION_SHARE":
                    for (Object arg : args) {
                        if (arg instanceof com.mycompany.jpademo.backend.dto.request.UpdateSessionShareRequest req) {
                            targetId = req.getSessionId();
                            String shareStatus = Boolean.TRUE.equals(req.getIsShared()) ? "công bố" : "gỡ công bố";
                            description = "Bác sĩ đã " + shareStatus + " ca chẩn đoán #" + targetId;
                            break;
                        }
                    }
                    break;

                case "UPDATE_CLINICAL_SYMPTOMS":
                    targetId = sessionId;
                    description = "Bác sĩ đã cập nhật triệu chứng lâm sàng cho ca chẩn đoán #" + sessionId;
                    break;

                default:
                    description = "Bác sĩ thực hiện hành động: " + action;
                    break;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
                currentUser = customUserDetails.getUser();
            }

            SystemLog systemLog = SystemLog.builder()
                    .user(currentUser)
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId != null ? targetId : (sessionId != null ? sessionId : 0))
                    .description(description)
                    .build();

            systemLogRepository.save(systemLog);
            log.info("Doctor log saved: {} - {}", action, description);

        } catch (Exception e) {
            log.error("Error saving doctor log: {}", e.getMessage());
        }
    }

    private String getStatusVietnamese(com.mycompany.jpademo.backend.enums.DiagnosisSessionStatus status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case PENDING: return "Chờ xử lý";
            case PROCESSING: return "Đang xử lý";
            case COMPLETED: return "Hoàn thành";
            case FAILED: return "Thất bại";
            default: return status.name();
        }
    }
}