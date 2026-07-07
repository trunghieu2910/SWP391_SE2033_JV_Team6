package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.request.BlockIpRequest;
import com.mycompany.jpademo.backend.dto.request.UnblockIpRequest;
import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.dto.response.SecurityStatsResponse;
import com.mycompany.jpademo.backend.entity.BlockedIP;
import com.mycompany.jpademo.backend.service.interfaces.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin/security")
@RequiredArgsConstructor
public class SecurityController {
    private final SecurityService securityService;
    private static final int TOP_LIMIT = 10;

    @GetMapping
    public String securityPage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        SecurityStatsResponse stats = securityService.getStats(startDateTime, endDateTime);
        List<IpRequestStats> topIps = securityService.getTopIps(TOP_LIMIT, startDateTime, endDateTime);
        List<EndpointRequestStats> topEndpoints = securityService.getTopEndpoints(TOP_LIMIT, startDateTime, endDateTime);
        List<BlockedIP> blockedIps = securityService.getBlockedIps(startDateTime, endDateTime);

        model.addAttribute("stats", stats);
        model.addAttribute("topIps", topIps);
        model.addAttribute("topEndpoints", topEndpoints);
        model.addAttribute("blockedIps", blockedIps);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        if (!model.containsAttribute("blockIpRequest")) {
            model.addAttribute("blockIpRequest", new BlockIpRequest());
        }

        return "admin/security";
    }

    @PostMapping("/block-ip")
    public String blockIp(
            @Valid @ModelAttribute("blockIpRequest") BlockIpRequest blockIpRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError("ipAddress") != null
                            ? bindingResult.getFieldError("ipAddress").getDefaultMessage()
                            : "Địa chỉ IP không hợp lệ");
            redirectAttributes.addFlashAttribute("blockIpRequest", blockIpRequest);
            return "redirect:/admin/security";
        }
        try {
            String adminUsername = "admin";
            securityService.blockIp(blockIpRequest, adminUsername);
            redirectAttributes.addFlashAttribute("success",
                    "Đã chặn IP " + blockIpRequest.getIpAddress() + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/security";
    }

    @PostMapping("/unlock-ip")
    public String unlockIp(
            @Valid @ModelAttribute UnblockIpRequest unblockIpRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError("ipAddress") != null
                            ? bindingResult.getFieldError("ipAddress").getDefaultMessage()
                            : "Địa chỉ IP không hợp lệ");
            return "redirect:/admin/security";
        }
        try {
            securityService.unblockIp(unblockIpRequest);
            redirectAttributes.addFlashAttribute("success",
                    "Đã bỏ chặn IP: " + unblockIpRequest.getIpAddress());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/security";
    }
}