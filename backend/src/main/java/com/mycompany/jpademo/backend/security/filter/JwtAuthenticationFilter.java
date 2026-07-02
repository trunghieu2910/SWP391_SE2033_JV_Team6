package com.mycompany.jpademo.backend.security.filter;

import com.mycompany.jpademo.backend.security.jwt.JwtService;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, 
                                    jakarta.servlet.http.HttpServletResponse response, 
                                    jakarta.servlet.FilterChain filterChain) 
            throws jakarta.servlet.ServletException, java.io.IOException {

        String token = null;

        // 1. Try to read from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.replace("Bearer ", "");
        }

        // 2. Try to read from Cookie 'token' if not found in header
        if (token == null && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        // Extract username
        String username = jwtService.extractUsername(token);

        if (username != null &&  SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load users from the database.
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Validate JWT
            if (jwtService.validateToken(token, userDetails)) {
                // Create an Authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Include in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}

