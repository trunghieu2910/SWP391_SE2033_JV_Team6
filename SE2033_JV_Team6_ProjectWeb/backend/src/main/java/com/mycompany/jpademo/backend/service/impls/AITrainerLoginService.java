package com.mycompany.jpademo.backend.service.impls;

import com.mycompany.jpademo.backend.security.jwt.JwtService;
import org.springframework.stereotype.Service;

@Service
public class AITrainerLoginService extends AdminLoginService {

    public AITrainerLoginService(JwtService jwtService) {
        super(jwtService);
    }
}
