package com.mycompany.jpademo.backend.service.interfaces;

public interface SystemLogService {

    void logActivity(String targetType, Integer targetID, String action, String description);
}
