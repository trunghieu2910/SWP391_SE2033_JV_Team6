package com.mycompany.jpademo.backend.controller;

import com.google.firebase.auth.FirebaseToken;
import com.mycompany.jpademo.backend.dto.request.GoogleCompleteRequest;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.service.interfaces.FirebaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class WebGoogleAuthController {

    private final FirebaseService firebaseService;   // tái dùng nguyên vẹn
    private final UserRepository userRepository;      // tái dùng nguyên vẹn
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> googleSession(
            @RequestBody Map<String, String> body,
            HttpServletRequest request, HttpServletResponse response) {

        FirebaseToken decoded = firebaseService.verifyIdToken(body.get("idToken"));
        String email = decoded.getEmail();

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Chưa có tài khoản → yêu cầu frontend hiện form nhập thêm
            return ResponseEntity.ok(Map.of(
                    "status", "NEED_MORE_INFO",
                    "email", email,
                    "fullName", decoded.getName() == null ? "" : decoded.getName()
            ));
        }

        User user = userOpt.get();
        if (user.getStatus() == UserStatus.BANNED) {
            return ResponseEntity.status(401).body(Map.of("status", "BANNED"));
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(401).body(Map.of("status", "LOCKED"));  
        }

        establishSession(user, request, response);
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "redirect", redirectByRole(user)
        ));
    }

    @PostMapping("/complete-session")
    public ResponseEntity<Map<String, String>> completeSession(
            @Valid @RequestBody GoogleCompleteRequest req,
            HttpServletRequest request, HttpServletResponse response) {

        FirebaseToken decoded = firebaseService.verifyIdToken(req.getIdToken());
        String email = decoded.getEmail();

        if (userRepository.existsByEmail(email))
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại."));
        // ... các check trùng userName/phone/nationalID giống completeGoogleRegistration cũ ...

        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT).orElseThrow();
        String rawPassword = UUID.randomUUID().toString().substring(0, 8) + "X@1a";

        User user = new User();
        user.setUserName(req.getUserName());
        user.setFullName(decoded.getName());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhoneNumber(req.getPhoneNumber());
        user.setNationalID(req.getNationalID());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(patientRole);
        userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patientRepository.save(patient);

        emailService.sendPasswordEmail(email, decoded.getName(), rawPassword);

        establishSession(user, request, response);
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
            case DOCTOR     -> "/doctor/sessions";
            case PATIENT    -> "/patient/home";
            case ADMIN      -> "/admin/dashboard";
            case PHARMACIST -> "/pharmacist/";
            case RECEPTIONIST -> "/receptionist/create-session";
            default -> "/";
        };
    }
}
