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

/**
 * Handles Google sign-in (a Firebase ID token sent up by the frontend after
 * the user signs in with Google in the browser). Replaces JWT entirely:
 * once the token is validated, a genuine Spring Security session is created
 * directly (exactly the same way as a form login), so the rest of the
 * system uses a single, unified session mechanism regardless of which
 * "login method" was used.
 */
@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class WebGoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final SystemLogService systemLogService;

    /**
     * STEP 1 — receives the idToken from Firebase and determines whether
     * this person already has an account in the system
     * (googleAuthService.resolveSession):
     *   - OK: the account already exists (this is the user's SECOND OR LATER
     *     Google login) -> the session is created immediately, a
     *     "GOOGLE_LOGIN" log entry is written, and the redirect URL is returned.
     *   - NEED_MORE_INFO: no account exists yet for this Google email
     *     (this is the FIRST-EVER Google login) -> NO session is created at
     *     this step; email/fullName are returned so the frontend can open
     *     the "complete your profile" form (see completeSession() below).
     *   - BANNED / INACTIVE / LOCKED: the account exists but isn't allowed
     *     to log in -> returns 401 with the corresponding status.
     */
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

    /**
     * STEP 2 (only reached when googleSession() returned NEED_MORE_INFO) —
     * the first-time Google user fills in the remaining required fields
     * (phone number, national ID, etc.); a new account is created in the DB
     * (googleAuthService.completeRegistration), and a session is established
     * right away, as if a normal login had just happened.
     * Logged separately as "GOOGLE_LOGIN_FIRST_TIME" (as opposed to the
     * regular "GOOGLE_LOGIN") to distinguish first-time signups from
     * returning Google logins — useful for reporting/audit purposes.
     */
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

    /**
     * Core helper shared by both flows above: manually builds an
     * Authentication + SecurityContext and persists it into the
     * HttpSession — this is the manual, non-form way of "logging someone
     * in", fully replacing the old JWT-issuing approach.
     */
    private void establishSession(User user, HttpServletRequest request, HttpServletResponse response) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
    }

    /** Looks up the home page for this user's Role — must stay in sync with
     *  the ROLE_HOME map in RoleBasedSuccessHandler (form login). If the two
     *  ever drift apart, Google users and form-login users with the same
     *  role will land on different home pages — worth checking periodically. */
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