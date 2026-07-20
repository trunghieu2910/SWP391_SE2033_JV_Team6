package com.mycompany.jpademo.backend.security.filter;

import com.mycompany.jpademo.backend.event.BlockedIpChangeEvent;
import com.mycompany.jpademo.backend.repository.BlockedIPRepository;
import com.mycompany.jpademo.backend.security.util.ClientIpResolver;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockedIpFilter extends OncePerRequestFilter {
    private final BlockedIPRepository blockedIPRepository;
    private final ClientIpResolver clientIpResolver;

    private volatile Set<String> blockedIpCache = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Scheduled(fixedRate = 30_000)
    private void refreshCache() {
        try {
            Set<String> lastest = blockedIPRepository.findAll()
                    .stream()
                    .map(b -> b.getIpAddress())
                    .collect(Collectors.toSet());
            this.blockedIpCache = lastest;
            log.info("Cập nhật danh sách IP bị chặn vào RAM thành công");
        } catch (Exception e) {
            log.error("Không thể tải danh sách IP bị chặn từ DB, giữ nguyên cache cũ", e);
        }
    }

    @EventListener
    public void handleBlockedIpChanged(BlockedIpChangeEvent event) {
        if (event.isBlocked()) {
            blockedIpCache.add(event.getIpAddress());
            log.info("Đã đồng bộ bộ nhớ RAM: Chặn IP ", event.getIpAddress());
        } else {
            blockedIpCache.remove(event.getIpAddress());
            log.info("Đã đồng bộ bộ nhớ RAM: Bỏ chặn IP ", event.getIpAddress());
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/auth/login") || path.startsWith("/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = clientIpResolver.resolve(request);
        if (blockedIpCache.contains(ipAddress)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"IP này đã bị chặn truy cập do vi phạm chính sách!\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
