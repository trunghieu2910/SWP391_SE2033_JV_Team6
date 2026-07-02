package com.mycompany.jpademo.backend.security.config;

import com.mycompany.jpademo.backend.security.filter.BlockedIpFilter;
import com.mycompany.jpademo.backend.security.filter.JwtAuthenticationFilter;
import com.mycompany.jpademo.backend.security.filter.RateLimitingFilter;
import com.mycompany.jpademo.backend.security.filter.RequestLoggingFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final BlockedIpFilter blockedIpFilter;
    private final RateLimitingFilter rateLimitingFilterl;
    private final RequestLoggingFilter requestLoggingFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(csrf -> csrf.disable())

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();
                            if (uri.startsWith("/api/")) {
                                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                String jsonResponse = String.format(
                                        "{\"success\": false, \"message\": \"%s\"}",
                                        "Vui lòng đăng nhập để thực hiện chức năng này!"
                                );
                                response.getWriter().write(jsonResponse);
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )

                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/register/verify-otp",
                                "/api/auth/register/resend-otp",
                                "/api/auth/forgot-password",
                                "/api/auth/forgot-password/verify-otp",
                                "/api/auth/forgot-password/reset-password",
                                "/api/auth/google",
                                "/api/auth/google/complete",
                                "/api/integration/lis/results").permitAll()

                        // Static resources — CSS/JS/images không cần auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // Cho phép truy cập trang login và logout Thymeleaf
                        .requestMatchers("/login", "/logout").permitAll()

                        // Thymeleaf pages cho bác sĩ — bảo vệ bằng @PreAuthorize trên controller
                        .requestMatchers("/doctor/medical-records", "/doctor/medical-records/**").authenticated()

                        .anyRequest().authenticated())

                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(blockedIpFilter, RequestLoggingFilter.class)
                .addFilterAfter(rateLimitingFilterl, BlockedIpFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RateLimitingFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
