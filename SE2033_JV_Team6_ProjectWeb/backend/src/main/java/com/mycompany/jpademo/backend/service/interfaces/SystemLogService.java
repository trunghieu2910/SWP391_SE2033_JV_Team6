package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.response.SystemLogRespone;

import java.util.List;

public interface SystemLogService {
    List<SystemLogRespone> getAllLogs();

    List<SystemLogRespone> getLogByUser(Integer userId);

    List<SystemLogRespone> searchLogs(String keyword);

    List<SystemLogRespone> getLogByAction(String action);
}
