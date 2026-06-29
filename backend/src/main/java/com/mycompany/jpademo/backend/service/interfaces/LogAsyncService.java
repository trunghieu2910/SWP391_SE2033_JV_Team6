package com.mycompany.jpademo.backend.service.interfaces;

public interface LogAsyncService {
    public void saveLogAsync(String ip, String uri, String method, String userAgent);
}
