package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.request.UnblockIpRequest;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.event.BlockedIpChangeEvent;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.exception.UnauthorizedActionException;
import com.mycompany.jpademo.backend.repository.BlockedIPRepository;
import com.mycompany.jpademo.backend.repository.RequestLogRepository;
import com.mycompany.jpademo.backend.security.util.ClientIpResolver;
import com.mycompany.jpademo.backend.service.interfaces.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Author: GiangLTHE194888
 * Task: Service implementation for system security auditing, monitoring IP traffic, and blocking malicious IPs.
 */
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {
    private final BlockedIPRepository blockedIPRepository;
    private final RequestLogRepository requestLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest httpServletRequest;
    private final ClientIpResolver clientIpResolver;

    /** Retrieves aggregated security stats for request limits. */
    @Override
    public SecurityStatsResponse getStats(LocalDateTime startDate, LocalDateTime endDate) {
        long totalRequests = requestLogRepository.countWithDateFilter(startDate, endDate);
        long totalBlockIps = blockedIPRepository.countByCreatedAtBetween(startDate, endDate);

        Double avgRequestsPerMinute = null;
        if (startDate != null && endDate != null) {
            avgRequestsPerMinute = requestLogRepository.getAvgRequestsPerMinute(startDate, endDate);
        }

        Double roundedAvg = avgRequestsPerMinute != null ?
                BigDecimal.valueOf(avgRequestsPerMinute)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue()
                : 0.0;

        return SecurityStatsResponse.builder()
                .totalRequestsToday(totalRequests)
                .totalBlockedIps(totalBlockIps)
                .avgRequestPerMinute(roundedAvg)
                .build();
    }

    /** Retrieves the most active IP addresses by request count. */
    @Override
    public List<IpRequestStats> getTopIps(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, limit);
        if (startDate != null || endDate != null) {
            return requestLogRepository.findTopIpsWithDateFilter(startDate, endDate, pageable);
        }
        return requestLogRepository.findTopIps(pageable);
    }

    /** Retrieves the most accessed endpoints by request count. */
    @Override
    public List<EndpointRequestStats> getTopEndpoints(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, limit);
        if (startDate != null || endDate != null) {
            return requestLogRepository.findTopEndpointsWithDateFilter(startDate, endDate, pageable);
        }
        return requestLogRepository.findTopEndpoints(pageable);
    }

    /** Retrieves a list of all currently blocked IP addresses. */
    @Override
    public List<BlockedIP> getBlockedIps(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return blockedIPRepository.findAll();
        }
        return blockedIPRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /** Blocks a specific IP address based on an administrator's decision. */
    @Override
    @Transactional
    @AdminActionLog(action = "BLOCKED_IP", targetType = "BlockedIP")
    public void blockIp(BlockIpRequest request, User admin) {
        if (!RoleName.ADMIN.equals(admin.getRole().getRoleName())) {
            throw new UnauthorizedActionException("Bạn không có quyền thực hiện hành động này");
        }
        String currentIp = clientIpResolver.resolve(httpServletRequest);
        if (request.getIpAddress().equals(currentIp)) {
            throw new BadRequestException("Không thể tự chặn địa chỉ IP hiện tại của chính bạn.");
        }
        List<String> sensitiveIps = List.of("127.0.0.1", "0.0.0.0", "0:0:0:0:0:0:0:1", "localhost");
        if (sensitiveIps.contains(request.getIpAddress().trim())) {
            throw new BadRequestException("Không thể chặn các địa chỉ IP nội bộ hoặc nhạy cảm của hệ thống.");
        }
        if (blockedIPRepository.existsById(request.getIpAddress())) {
            throw new BadRequestException("IP " + request.getIpAddress() + " đã bị chặn trước đó.");
        }
        BlockedIP blockedIP = BlockedIP.builder()
                .ipAddress(request.getIpAddress())
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .createdBy(admin.getFullName())
                .build();
        blockedIPRepository.save(blockedIP);

        eventPublisher.publishEvent(new BlockedIpChangeEvent(request.getIpAddress(), true));
    }

    /** Unblocks a previously blocked IP address. */
    @Override
    @AdminActionLog(action = "UNBLOCKED_IP", targetType = "BlockedIP")
    public void unblockIp(UnblockIpRequest request, User admin) {
        if (!RoleName.ADMIN.equals(admin.getRole().getRoleName())) {
            throw new UnauthorizedActionException("Bạn không có quyền thực hiện hành động này");
        }
        String ipAddress = request.getIpAddress();
        BlockedIP blockedIP = blockedIPRepository.findById(ipAddress).orElseThrow(
                () -> new BadRequestException("IP " + ipAddress + " không tồn tại trong danh sách chặn.")
        );
        blockedIPRepository.deleteById(ipAddress);

        eventPublisher.publishEvent(new BlockedIpChangeEvent(request.getIpAddress(), false));
    }
}
