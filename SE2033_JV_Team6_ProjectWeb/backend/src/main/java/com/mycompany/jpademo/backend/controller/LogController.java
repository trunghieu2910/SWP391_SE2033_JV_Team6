package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.SystemLogRespone;
import com.mycompany.jpademo.backend.service.interfaces.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class LogController {
    private final SystemLogService systemLogService;

    @GetMapping("/logs")
    public List<SystemLogRespone> getAllLogs() {
        return systemLogService.getAllLogs();
    }

    @GetMapping("/logs/user/{userId}")
    public List<SystemLogRespone> getLogByUser(@PathVariable Integer userId) {
        return systemLogService.getLogByUser(userId);
    }

    @GetMapping("/logs/search")
    public List<SystemLogRespone> searchLogs(@RequestParam String keyword) {
        return systemLogService.searchLogs(keyword);
    }

    @GetMapping("/logs/action/{action}")
    public List<SystemLogRespone> getLogByAction(@PathVariable String action) {
        return systemLogService.getLogByAction(action);
    }
}
