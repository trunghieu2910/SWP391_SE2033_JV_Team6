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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
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
                            String jsonResponse = String.format(
                                    "{\"success\": false, \"message\": \"%s\"}",
                                    "Vui lòng đăng nhập để thực hiện chức năng này!"
                            );
                            String acceptHeader = request.getHeader("Accept");
                            String requestUri = request.getRequestURI();
                            boolean jsonRequest = requestUri.startsWith("/api/")
                                    || (acceptHeader != null && acceptHeader.contains("application/json"));

                            if (jsonRequest) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write(jsonResponse);
                            } else {
                                new LoginUrlAuthenticationEntryPoint("/auth/login")
                                        .commence(request, response, authException);
                            }
                        })
                )

                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/register/verify-otp",
                                "/api/auth/register/resend-otp",
                                "/api/auth/forgot-password",
                                "/api/auth/forgot-password/verify-otp",
                                "/api/auth/forgot-password/reset-password",
                                "/api/auth/google",
                                "/api/auth/google/complete",
                                "/api/integration/lis/results",
                                "/auth/**",
                                "/forgot-password/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/patient/css/**",
                                "/patient/js/**",
                                "/patient/images/**"
                        ).permitAll()

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
