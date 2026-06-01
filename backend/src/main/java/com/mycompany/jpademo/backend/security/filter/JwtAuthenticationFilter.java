package com.mycompany.jpademo.backend.security.filter;

import com.mycompany.jpademo.backend.security.jwt.JwtService;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            jakarta.servlet.FilterChain filterChain)
            throws jakarta.servlet.ServletException, java.io.IOException {

        System.out.println("========== JWT FILTER RUNNING ==========");

        String authHeader = request.getHeader("Authorization");

        System.out.println("Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer Token Found");
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String token = authHeader.substring(7);

            System.out.println("Token: " + token);

            String username = jwtService.extractUsername(token);

            System.out.println("Username From Token: " + username);

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        customUserDetailsService.loadUserByUsername(username);

                System.out.println("User Loaded: "
                        + userDetails.getUsername());

                System.out.println("Authorities: "
                        + userDetails.getAuthorities());

                boolean valid =
                        jwtService.validateToken(token, userDetails);

                System.out.println("Token Valid: " + valid);

                if (valid) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    System.out.println(
                            "Authentication Successfully Set");
                }
            }

        } catch (Exception e) {
            System.out.println("JWT ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println(
                "Security Context Authentication: "
                        + SecurityContextHolder.getContext().getAuthentication()
        );

        filterChain.doFilter(request, response);
    }

}
