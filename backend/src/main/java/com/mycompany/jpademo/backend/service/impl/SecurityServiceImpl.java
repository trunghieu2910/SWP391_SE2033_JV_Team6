package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.repository.BlockedIPRepository;
import com.mycompany.jpademo.backend.repository.RequestLogRepository;
import com.mycompany.jpademo.backend.service.interfaces.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public SecurityStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long totalDay = requestLogRepository.countByTimestampAfter(startOfDay);
        long blockedCount = blockedIPRepository.count();

        long minutesElapsed = java.time.Duration.between(startOfDay, LocalDateTime.now()).toMinutes();
        if (minutesElapsed < 1) {
            minutesElapsed = 1;
        }

        double avgPerMinute = (double) totalDay / minutesElapsed;

        return SecurityStatsResponse.builder()
                .totalRequestsToday(totalDay)
                .totalBlockedIps(blockedCount)
                .avgRequestPerMinute(Math.round(avgPerMinute * 100.0) / 100.0)
                .build();
    }

    @Override
    public List<IpRequestStats> getTopIps(int limit) {
        return requestLogRepository.findTopIps(PageRequest.of(0, limit));
    }

    @Override
    public List<EndpointRequestStats> getTopEndpoints(int limit) {
        return requestLogRepository.findTopEndpoints(PageRequest.of(0, limit));
    }

    @Override
    public List<BlockedIP> getBlockedIps() {
        return blockedIPRepository.findAll();
    }

    @Override
    @Transactional
    public void blockIp(BlockIpRequest request, String adminUsername) {
        BlockedIP blockedIP = BlockedIP.builder()
                .ipAddress(request.getIpAddress())
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .createdBy(adminUsername)
                .build();
        blockedIPRepository.save(blockedIP);
    }

    @Override
    public void unblockIp(String ipAddress) {
        blockedIPRepository.deleteById(ipAddress);
    }
}
