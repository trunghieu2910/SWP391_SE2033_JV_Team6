package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityService {
    SecurityStatsResponse getStats(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<IpRequestStats> getTopIps(int limit, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<EndpointRequestStats> getTopEndpoints(int limit, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<BlockedIP> getBlockedIps(LocalDateTime startDateTime, LocalDateTime endDateTime);

    void blockIp(BlockIpRequest request, String adminUsername);

    void unblockIp(String ipAddress);
}