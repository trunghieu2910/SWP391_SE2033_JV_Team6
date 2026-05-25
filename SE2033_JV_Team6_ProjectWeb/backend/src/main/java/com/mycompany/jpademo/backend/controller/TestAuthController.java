package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.security.jwt.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestAuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public TestAuthController(
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> generateTestToken(
            @RequestParam String email,
            @RequestParam String role
    ) {
        String token = jwtService.generateToken(email, role);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hash")
    public ResponseEntity<Map<String, String>> hashPassword(
            @RequestParam String password
    ) {
        String hash = passwordEncoder.encode(password);

        Map<String, String> response = new HashMap<>();
        response.put("rawPassword", password);
        response.put("bcryptHash", hash);

        return ResponseEntity.ok(response);
    }
}