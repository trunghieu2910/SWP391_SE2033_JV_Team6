package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;

import java.util.List;

import java.time.LocalDate;

public interface SecurityService {
    SecurityStatsResponse getStats(LocalDate startDate, LocalDate endDate);
    List<IpRequestStats> getTopIps(int limit, LocalDate startDate, LocalDate endDate);
    List<EndpointRequestStats> getTopEndpoints(int limit, LocalDate startDate, LocalDate endDate);
    List<BlockedIP> getBlockedIps();
    void blockIp(BlockIpRequest request, String adminUsername);
    void unblockIp(String ipAddress);
}