package com.mycompany.jpademo.backend.service.impl;

import com.google.firebase.auth.FirebaseToken;
import com.mycompany.jpademo.backend.aop.annotation.LogActivity;
import com.mycompany.jpademo.backend.dto.request.*;
import com.mycompany.jpademo.backend.dto.response.GoogleLoginResponse;
import com.mycompany.jpademo.backend.dto.response.LoginResponse;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.exception.UserNotFoundException;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import com.mycompany.jpademo.backend.service.interfaces.FirebaseService;
import com.mycompany.jpademo.backend.service.factory.AuthFactory;
import com.mycompany.jpademo.backend.service.interfaces.AuthService;
import com.mycompany.jpademo.backend.service.interfaces.LoginStrategy;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mycompany.jpademo.backend.exception.ResourceAlreadyExistsException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.exception.InvalidOtpException;
import com.mycompany.jpademo.backend.util.OtpUtil;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthFactory authFactory;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final FirebaseService firebaseService;

    @Override
    @LogActivity(action = "LOGIN", targetType = "Users", description = "Người dùng đăng nhập")
    public LoginResponse login(LoginRequest request) {
        // Verify login/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Query the user again to retrieve the full information.
        User user = userRepository.findByEmailOrUsernameOrPhoneNumberOrNationalId(
                request.getLogin(),
                request.getLogin(),
                request.getLogin(),
                request.getLogin()
        ).orElseThrow(() -> new UserNotFoundException("Thông tin đăng nhập không hợp lệ"));

        // Choose a strategy based on role - convert RoleName enum to String
        LoginStrategy strategy = authFactory.getLoginStrategy(user.getRole().getRoleName().name());

        return strategy.login(user);
    }

    @Override
    @LogActivity(action = "LOGOUT", targetType = "Users", description = "Người dùng đăng xuất")
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User currentUser = userDetails.getUser();

            currentUser.setLastLogoutTime(LocalDateTime.now());
            userRepository.save(currentUser);
        }

        SecurityContextHolder.clearContext();
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new ResourceAlreadyExistsException("Username đang được sử dụng.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email đang được sử dụng.");
        }
        if (userRepository.existsByNationalID(request.getNationalID())) {
            throw new ResourceAlreadyExistsException("Số CMND/CCCD đã tồn tại.");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Số điện thoại đang được sử dụng.");
        }

        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT không tồn tại."));

        User user = new User();
        user.setUserName(request.getUserName());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalID(request.getNationalID());
        user.setStatus(UserStatus.PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(patientRole);

        userRepository.save(user);

        Patient patient = new Patient();
        patient.setGender(request.getGender());
        patient.setDob(request.getDob());
        patient.setAddress(request.getAddress());
        patient.setUser(user);
        
        patientRepository.save(patient);

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    public void verifyRegistrationOtp(OtpVerificationRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new InvalidOtpException("no");
        }

        boolean valid = OtpUtil.verifyOtp(user.getEmail(), request.getOtp());
        if (!valid) {
            throw new InvalidOtpException("no");
        }

        OtpUtil.removeOtp(user.getEmail());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public void resendRegistrationOtp(ResendOtpRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new InvalidOtpException("no");
        }

        String otp = OtpUtil.generateOtp();
        OtpUtil.saveOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    @LogActivity(action = "GOOGLE_LOGIN", targetType = "User", description = "Đăng nhập bằng Google")
    public Object handleGoogleLogin(String idToken) {
        // 1. Verify token với Firebase (bạn cần đảm bảo FirebaseService trả về đối tượng có chứa email và name)
        FirebaseToken decodedToken = firebaseService.verifyIdToken(idToken);
        String email = decodedToken.getEmail();
        String fullName = decodedToken.getName();

        // 2. Tìm User trong DB
        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Nếu bị ban thì ném lỗi (sử dụng Exception có sẵn của bạn hoặc tạo mới ForbiddenException)
            if (user.getStatus() == UserStatus.BANNED) {
                throw new com.mycompany.jpademo.backend.exception.UnauthorizedActionException("Tài khoản đã bị khóa. Không thể đăng nhập.");
            }

            // 2. Đặt authentication vào security context để duy trì session cho các request Thymeleaf
            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Tái sử dụng Strategy để sinh JWT Token
            LoginStrategy strategy = authFactory.getLoginStrategy(user.getRole().getRoleName().name());
            return strategy.login(user);
        } else {
            // 4. Trả về thông tin yêu cầu frontend nhập thêm
            GoogleLoginResponse response = new GoogleLoginResponse();
            response.setStatus("NEED_MORE_INFO");
            response.setEmail(email);
            response.setFullName(fullName);

            return response;
        }
    }

    @Override
    @LogActivity(action = "GOOGLE_REGISTER", targetType = "User", description = "Đăng ký bằng Google")
    public LoginResponse completeGoogleRegistration(GoogleCompleteRequest request) {
        // 1. Verify token lần nữa để lấy lại email chính chủ
        FirebaseToken decodedToken = firebaseService.verifyIdToken(request.getIdToken());
        String email = decodedToken.getEmail();
        String fullName = decodedToken.getName();

        // 2. Kiểm tra trùng lặp dữ liệu
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email đã tồn tại trong hệ thống.");
        }
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new ResourceAlreadyExistsException("Username đã tồn tại.");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Số điện thoại đã tồn tại");
        }
        if (userRepository.existsByNationalID(request.getNationalID())) {
            throw new ResourceAlreadyExistsException("Số CMND/CCCD đã tồn tại.");
        }

        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT không tồn tại."));

        // 3. Sinh mật khẩu ngẫu nhiên & Hash
        String rawPassword = generateRandomPassword();
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // 4. Tạo User
        User user = new User();
        user.setUserName(request.getUserName());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalID(request.getNationalID());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastChangePassTime(LocalDateTime.now());
        user.setRole(patientRole);

        userRepository.save(user);

        // 5. Tạo Patient liên kết (các trường khác rỗng để update sau)
        Patient patient = new Patient();
        patient.setUser(user);
        patientRepository.save(patient);

        // 6. Gửi Email chứa mật khẩu cho user
        // Chú ý: Bạn nhớ định nghĩa hàm sendPasswordEmail bên trong EmailService nhé
        emailService.sendPasswordEmail(email, fullName, rawPassword);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // 2. Tạo một thẻ Authentication (Đóng dấu hợp lệ)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Không cần password ở bước này vì đã xác thực qua Google
                        userDetails.getAuthorities()
                );

        // 3. Đặt thẻ vào túi (SecurityContext)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7. Sinh JWT Token và trả về ngay để Frontend đăng nhập luôn
        LoginStrategy strategy = authFactory.getLoginStrategy(user.getRole().getRoleName().name());
        return strategy.login(user);
    }

    // Hàm phụ trợ sinh mật khẩu 12 ký tự đảm bảo độ mạnh (Chữ hoa, thường, số, ký tự đặc biệt)
    private String generateRandomPassword() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // Kết hợp 8 ký tự ngẫu nhiên từ UUID + 4 ký tự cứng cứng chuẩn quy tắc
        return uuid.substring(0, 8) + "X@1a";
    }
}
