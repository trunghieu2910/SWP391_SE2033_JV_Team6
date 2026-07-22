package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.GoogleCompleteRequest;
import com.mycompany.jpademo.backend.dto.response.GoogleSessionResult;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.GoogleAuthService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class WebGoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final SystemLogService systemLogService;

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> googleSession(
            @RequestBody Map<String, String> body,
            HttpServletRequest request, HttpServletResponse response) {

        GoogleSessionResult result = googleAuthService.resolveSession(body.get("idToken"));

        return switch (result.getStatus()) {
            case NEED_MORE_INFO -> ResponseEntity.ok(Map.of(
                    "status", "NEED_MORE_INFO",
                    "email", result.getEmail(),
                    "fullName", result.getFullName()
            ));
            case BANNED -> ResponseEntity.status(401).body(Map.of("status", "BANNED"));
            case INACTIVE -> ResponseEntity.status(401).body(Map.of("status", "INACTIVE"));
            case LOCKED -> ResponseEntity.status(401).body(Map.of("status", "LOCKED"));
            case OK -> {
                establishSession(result.getUser(), request, response);

                systemLogService.logActivity("Users", result.getUser().getUserId(), "GOOGLE_LOGIN",
                        "Người dùng đăng nhập bằng Google (" + result.getUser().getEmail() + ")");

                yield ResponseEntity.ok(Map.of(
                        "status", "OK",
                        "redirect", redirectByRole(result.getUser())
                ));
            }
        };
    }

    @PostMapping("/complete-session")
    public ResponseEntity<Map<String, String>> completeSession(
            @Valid @RequestBody GoogleCompleteRequest req,
            HttpServletRequest request, HttpServletResponse response) {

        User user = googleAuthService.completeRegistration(req);
        establishSession(user, request, response);

        systemLogService.logActivity("Users", user.getUserId(), "GOOGLE_LOGIN_FIRST_TIME",
                "Người dùng đăng nhập bằng Google lần đầu tiên, tài khoản mới được tạo (" + user.getEmail() + ")");

        return ResponseEntity.ok(Map.of("status", "OK", "redirect", redirectByRole(user)));
    }

    // ── Hàm lõi: tạo session thủ công, thay cho việc sinh JWT ──
    private void establishSession(User user, HttpServletRequest request, HttpServletResponse response) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
    }

    private String redirectByRole(User user) {
        return switch (user.getRole().getRoleName()) {
            case DOCTOR       -> "/doctor/sessions";
            case PATIENT      -> "/patient/home";
            case ADMIN        -> "/admin/dashboard";
            case PHARMACIST   -> "/pharmacist/";
            case RECEPTIONIST -> "/receptionist/create-session";
            default -> "/";
        };
    }
}