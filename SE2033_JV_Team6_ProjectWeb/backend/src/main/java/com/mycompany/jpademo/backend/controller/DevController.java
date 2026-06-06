package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.security.jwt.JwtService;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @GetMapping("/token/{username}")
    public String getToken(@PathVariable String username) {

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(username);

        return jwtService.generateToken(userDetails);
    }
}
