package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.service.interfaces.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SecurityController {
    private final SecurityService securityService;

    @GetMapping("/stats")
    public ResponseEntity<SecurityStatsResponse> getStats() {
        return ResponseEntity.ok(securityService.getStats());
    }

    @GetMapping("/top-ips")
    public ResponseEntity<List<IpRequestStats>> getTopIps(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(securityService.getTopIps(limit));
    }

    @GetMapping("/top-endpoints")
    public ResponseEntity<List<EndpointRequestStats>> getTopEndpoints(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(securityService.getTopEndpoints(limit));
    }

    @GetMapping("/blocked-ips")
    public ResponseEntity<List<BlockedIP>> getBlockedIps() {
        return ResponseEntity.ok(securityService.getBlockedIps());
    }

    @PostMapping("/block-ip")
    public ResponseEntity<ApiResponse> blockIp(@Valid @RequestBody BlockIpRequest blockIpRequest,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        securityService.blockIp(blockIpRequest, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Đã chặn địa chỉ IP: " + blockIpRequest.getIpAddress() + " thành công.")
                        .build());
    }

    @DeleteMapping("/unlock-ip/{ip}")
    public ResponseEntity<ApiResponse> unlockIp(@PathVariable String ip) {
        securityService.unblockIp(ip);
        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Đã bỏ chặn địa chỉ IP: " + ip + " thành công.")
                        .build());
    }
}
