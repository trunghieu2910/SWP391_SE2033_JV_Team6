package com.mycompany.jpademo.backend.security.filter;

import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, IpAddressCount> requestCache = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/error") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.startsWith("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = getClientIp(request);
        long currentMinute = System.currentTimeMillis() / 60000;

        IpAddressCount count = requestCache.compute(ipAddress, (k, v) -> {
            if (v == null || v.minute != currentMinute) {
                return new IpAddressCount(currentMinute, new AtomicInteger(1));
            }
            int newCount = v.count.incrementAndGet();
            return v;
        });

        int currentCount = count.count.get();

        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            HttpSession session = request.getSession();
            String originalUrl = request.getRequestURI();
            String queryString = request.getQueryString();

            if (queryString != null && !queryString.isEmpty()) {
                originalUrl += "?" + queryString;
            }
            session.setAttribute("originalUrl", originalUrl);

            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
                    String role = userDetails.getUser().getRole().getRoleName().name();
                    session.setAttribute("userRole", role);
                }
            } catch (Exception e) {
            }

            requestCache.remove(ipAddress);

            String acceptHeader = request.getHeader("Accept");
            String requestUri = request.getRequestURI();
            boolean isApiRequest = requestUri.startsWith("/api/")
                    || (acceptHeader != null && acceptHeader.contains("application/json"));

            if (isApiRequest) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"Quá nhiều yêu cầu gửi lên hệ thống. Vui lòng thử lại sau ít phút!\"}");
            } else {
                String encodedUrl = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
                response.sendRedirect("/error/rate-limit?redirect=" + encodedUrl);
            }
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private static class IpAddressCount {
        long minute;
        AtomicInteger count;

        IpAddressCount(long minute, AtomicInteger count) {
            this.minute = minute;
            this.count = count;
        }
    }
}