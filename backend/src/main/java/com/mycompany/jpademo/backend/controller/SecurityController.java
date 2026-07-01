package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.service.interfaces.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Controller
@RequestMapping("/admin/security")
@RequiredArgsConstructor
public class SecurityController {
    private final SecurityService securityService;

    @GetMapping
    public String securityPage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        model.addAttribute("stats", securityService.getStats(startDate, endDate));
        model.addAttribute("blockedIps", securityService.getBlockedIps());
        model.addAttribute("topIps", securityService.getTopIps(10, startDate, endDate));
        model.addAttribute("topEndpoints", securityService.getTopEndpoints(10, startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/security";
    }

    @PostMapping("/block-ip")
    public String blockIp(
            @RequestParam String ipAddress,
            @RequestParam String reason,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            BlockIpRequest request = new BlockIpRequest();
            request.setIpAddress(ipAddress);
            request.setReason(reason);
            String username = (userDetails != null) ? userDetails.getUsername() : "system_admin";
            securityService.blockIp(request, username);
            redirectAttributes.addFlashAttribute("success", "Đã chặn IP: " + ipAddress);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/security";
    }

    @PostMapping("/unlock-ip")
    public String unlockIp(@RequestParam String ipAddress, RedirectAttributes redirectAttributes) {
        try {
            securityService.unblockIp(ipAddress);
            redirectAttributes.addFlashAttribute("success", "Đã bỏ chặn IP: " + ipAddress);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/security";
    }
}