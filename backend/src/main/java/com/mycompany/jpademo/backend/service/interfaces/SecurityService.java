package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.UnblockIpRequest;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Author: GiangLTHE194888
 * Task: Service interface defining operations for system security auditing, monitoring IP traffic, and blocking malicious IPs.
 */
public interface SecurityService {
    /** Retrieves aggregated security stats for request limits. */
    SecurityStatsResponse getStats(LocalDateTime startDateTime, LocalDateTime endDateTime);

    /** Retrieves the most active IP addresses by request count. */
    List<IpRequestStats> getTopIps(int limit, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /** Retrieves the most accessed endpoints by request count. */
    List<EndpointRequestStats> getTopEndpoints(int limit, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /** Retrieves a list of all currently blocked IP addresses. */
    List<BlockedIP> getBlockedIps(LocalDateTime startDateTime, LocalDateTime endDateTime);

    /** Blocks a specific IP address based on an administrator's decision. */
    void blockIp(BlockIpRequest request, User admin);

    /** Unblocks a previously blocked IP address. */
    void unblockIp(UnblockIpRequest request, User admin);
}