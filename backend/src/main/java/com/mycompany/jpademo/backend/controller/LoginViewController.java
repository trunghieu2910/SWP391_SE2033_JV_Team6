package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.LoginRequest;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginViewController {

    private final AuthService authService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {

        try {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setLogin(username);
            loginRequest.setPassword(password);

            LoginResponse loginResponse = authService.login(loginRequest);

            // Set cookie containing the JWT token
            Cookie tokenCookie = new Cookie("token", loginResponse.getAccessToken());
            tokenCookie.setHttpOnly(true);
            tokenCookie.setSecure(false); // set to true if using HTTPS
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(86400); // 24 hours in seconds
            response.addCookie(tokenCookie);

            // Redirect user depending on their role
            String role = loginResponse.getRole();
            if (role != null && (role.equalsIgnoreCase("DOCTOR") || role.equalsIgnoreCase("ROLE_DOCTOR"))) {
                return "redirect:/doctor/medical-records";
            }

            // Fallback redirect
            return "redirect:/doctor/medical-records";

        } catch (Exception e) {
            model.addAttribute("error", "Tài khoản hoặc mật khẩu không chính xác!");
            model.addAttribute("username", username);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpServletResponse response) {
        // Expire the cookie
        Cookie tokenCookie = new Cookie("token", "");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);

        return "redirect:/login?logout";
    }
}
