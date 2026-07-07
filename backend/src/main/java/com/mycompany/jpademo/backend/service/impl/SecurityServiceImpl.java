package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.aop.annotation.AdminActionLog;
import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.request.UnblockIpRequest;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.exception.BadRequestException;
import com.mycompany.jpademo.backend.repository.BlockedIPRepository;
import com.mycompany.jpademo.backend.repository.RequestLogRepository;
import com.mycompany.jpademo.backend.service.interfaces.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {
    private final BlockedIPRepository blockedIPRepository;
    private final RequestLogRepository requestLogRepository;

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

    @Override
    public List<IpRequestStats> getTopIps(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, limit);
        if (startDate != null || endDate != null) {
            return requestLogRepository.findTopIpsWithDateFilter(startDate, endDate, pageable);
        }
        return requestLogRepository.findTopIps(pageable);
    }

    @Override
    public List<EndpointRequestStats> getTopEndpoints(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, limit);
        if (startDate != null || endDate != null) {
            return requestLogRepository.findTopEndpointsWithDateFilter(startDate, endDate, pageable);
        }
        return requestLogRepository.findTopEndpoints(pageable);
    }

    @Override
    public List<BlockedIP> getBlockedIps(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return blockedIPRepository.findAll();
        }
        return blockedIPRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    @Transactional
    @AdminActionLog(action = "BLOCKED_IP", targetType = "BlockedIP")
    public void blockIp(BlockIpRequest request, String adminUsername) {
        if (blockedIPRepository.existsById(request.getIpAddress())) {
            throw new BadRequestException("IP " + request.getIpAddress() + " đã bị chặn trước đó.");
        }
        BlockedIP blockedIP = BlockedIP.builder()
                .ipAddress(request.getIpAddress())
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .createdBy(adminUsername)
                .build();
        blockedIPRepository.save(blockedIP);
    }

    @Override
    @AdminActionLog(action = "UNBLOCKED_IP", targetType = "BlockedIP")
    public void unblockIp(UnblockIpRequest request) {
        String ipAddress = request.getIpAddress();
        BlockedIP blockedIP = blockedIPRepository.findById(ipAddress).orElseThrow(
                () -> new BadRequestException("IP " + ipAddress + " không tồn tại trong danh sách chặn.")
        );
        blockedIPRepository.deleteById(ipAddress);
    }
}
