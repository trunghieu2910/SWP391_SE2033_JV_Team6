package com.mycompany.jpademo.backend.service.factory;

import com.mycompany.jpademo.backend.service.impls.AdminLoginService;
import com.mycompany.jpademo.backend.service.impls.AITrainerLoginService;
import com.mycompany.jpademo.backend.service.impls.DoctorLoginService;
import com.mycompany.jpademo.backend.service.impls.PatientLoginService;
import com.mycompany.jpademo.backend.service.interfaces.LoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFactory {

    private final AdminLoginService adminLoginService;
    private final DoctorLoginService doctorLoginService;
    private final PatientLoginService patientLoginService;
    private final AITrainerLoginService aiTrainerLoginService;

    public LoginStrategy getLoginStrategy(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> adminLoginService;
            case "DOCTOR" -> doctorLoginService;
            case "PATIENT" -> patientLoginService;
            case "AITRAINER" -> aiTrainerLoginService;
            default -> throw new RuntimeException("Invalid role: " + role);
        };
    }
}
