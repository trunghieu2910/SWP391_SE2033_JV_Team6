package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.security.jwt.JwtService;
import org.springframework.stereotype.Service;

@Service
public class PatientLoginService extends AdminLoginService {

    public PatientLoginService(JwtService jwtService) {
        super(jwtService);
    }
}
