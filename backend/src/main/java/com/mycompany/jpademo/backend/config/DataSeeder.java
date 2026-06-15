package com.mycompany.jpademo.backend.config;

import com.mycompany.jpademo.backend.entity.Role;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import com.mycompany.jpademo.backend.repository.RoleRepository;
import com.mycompany.jpademo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(RoleName.ADMIN)
                        .build()));
        if (userRepository.findByUserName("admin_system").isEmpty()) {
            User admin = User.builder()
                    .userName("admin_system")
                    .fullName("System Administrator")
                    .email("admin_system@example.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .status(UserStatus.ACTIVE)
                    .role(adminRole)
                    .build();
            userRepository.save(admin);
            System.out.println("[DataSeeder] Admin user 'admin_system' created.");
        }
    }
}
