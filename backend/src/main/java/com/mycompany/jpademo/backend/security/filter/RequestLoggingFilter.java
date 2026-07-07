package com.mycompany.jpademo.backend.security.filter;

import com.mycompany.jpademo.backend.service.interfaces.LogAsyncService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {
    private final LogAsyncService logAsyncService;

    // Danh sách các extension static resource cần bỏ qua
    private static final List<String> STATIC_EXTENSIONS = Arrays.asList(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg",
            ".ico", ".woff", ".woff2", ".ttf", ".eot", ".map", ".webp"
    );

    // Danh sách các đường dẫn static resource cần bỏ qua
    private static final List<String> STATIC_PATHS = Arrays.asList(
            "/css/", "/js/", "/images/", "/img/", "/fonts/",
            "/webjars/", "/assets/", "/static/", "/public/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (!isStaticResource(uri)) {
            logAsyncService.saveLogAsync(
                    getClientIp(request),
                    uri,
                    request.getMethod(),
                    request.getHeader("User-Agent")
            );
        }
        filterChain.doFilter(request, response);
    }

    private boolean isStaticResource(String uri) {
        for (String ext : STATIC_EXTENSIONS) {
            if (uri.endsWith(ext)) {
                return true;
            }
        }

        for (String path : STATIC_PATHS) {
            if (uri.contains(path)) {
                return true;
            }
        }

        return false;
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
}