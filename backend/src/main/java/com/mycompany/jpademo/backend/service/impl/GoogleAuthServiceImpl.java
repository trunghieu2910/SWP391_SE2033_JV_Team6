package com.mycompany.jpademo.backend.service.impl;

import com.google.firebase.auth.FirebaseToken;
import com.mycompany.jpademo.backend.dto.request.GoogleCompleteRequest;
import com.mycompany.jpademo.backend.dto.response.GoogleSessionResult;
import com.mycompany.jpademo.backend.entity.Patient;
import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.exception.ResourceAlreadyExistsException;
import com.mycompany.jpademo.backend.exception.ResourceNotFoundException;
import com.mycompany.jpademo.backend.repository.PatientRepository;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import com.mycompany.jpademo.backend.service.interfaces.EmailService;
import com.mycompany.jpademo.backend.service.interfaces.FirebaseService;
import com.mycompany.jpademo.backend.service.interfaces.GoogleAuthService;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Default implementation of {@link GoogleAuthService}. Owns every
 * repository/data-access call for the Google sign-in feature, so that
 * WebGoogleAuthController stays a thin, servlet-facing layer.
 */
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final FirebaseService firebaseService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * {@inheritDoc}
     * <p>
     * Looks the verified email up in the database:
     * <ul>
     *   <li>no matching user -> {@code NEED_MORE_INFO} (first-time sign-in)</li>
     *   <li>user exists but BANNED / INACTIVE / temporarily LOCKED -> rejected</li>
     *   <li>otherwise -> {@code OK}, wrapping the existing user</li>
     * </ul>
     */
    @Override
    public GoogleSessionResult resolveSession(String idToken) {
        FirebaseToken decoded = firebaseService.verifyIdToken(idToken);
        String email = decoded.getEmail();

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return GoogleSessionResult.needMoreInfo(
                    email, decoded.getName() == null ? "" : decoded.getName());
        }

        User user = userOpt.get();
        if (user.getStatus() == UserStatus.BANNED) {
            return GoogleSessionResult.rejected(GoogleSessionResult.Status.BANNED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            return GoogleSessionResult.rejected(GoogleSessionResult.Status.INACTIVE);
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return GoogleSessionResult.rejected(GoogleSessionResult.Status.LOCKED);
        }

        return GoogleSessionResult.ok(user);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Re-verifies the Firebase ID token (never trusts the email/name the
     * client claims), rejects duplicate email/username/phone/national ID,
     * then creates the {@link User} + {@link Patient} pair with an
     * auto-generated password, and emails that password to the user.
     *
     * @throws ResourceAlreadyExistsException if the email, username, phone
     *                                          number, or national ID is
     *                                          already registered
     * @throws ResourceNotFoundException if the PATIENT role is missing from
     *                                     the Roles table (configuration error)
     */
    @Override
    @Transactional
    public User completeRegistration(GoogleCompleteRequest request) {
        FirebaseToken decoded = firebaseService.verifyIdToken(request.getIdToken());
        String email = decoded.getEmail();

        if (userRepository.existsByEmail(email))
            throw new ResourceAlreadyExistsException("Email đã tồn tại.");
        if (userRepository.existsByUserName(request.getUserName()))
            throw new ResourceAlreadyExistsException("Tên đăng nhập đã tồn tại.");
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber()))
            throw new ResourceAlreadyExistsException("Số điện thoại đã được sử dụng.");
        if (userRepository.existsByNationalID(request.getNationalID()))
            throw new ResourceAlreadyExistsException("Số CCCD đã được sử dụng.");

        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT không tồn tại."));

        String rawPassword = UUID.randomUUID().toString().substring(0, 8)
                + UUID.randomUUID().toString().substring(0, 4) + "@1a";

        User user = new User();
        user.setUserName(request.getUserName());
        user.setFullName(decoded.getName());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalID(request.getNationalID());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(patientRole);
        userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setGender(request.getGender());
        patient.setDob(request.getDob());

        String fullAddress = request.getAddressDetail() + ", " + request.getDistrict() + ", " + request.getProvince();
        patient.setAddress(fullAddress);

        patientRepository.save(patient);

        emailService.sendPasswordEmail(email, decoded.getName(), rawPassword);

        return user;
    }
}