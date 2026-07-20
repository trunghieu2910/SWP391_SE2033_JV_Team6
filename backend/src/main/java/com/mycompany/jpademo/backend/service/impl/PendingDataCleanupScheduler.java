package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.cache.PendingDoctorStore;
import com.mycompany.jpademo.backend.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingDataCleanupScheduler {
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanup() {
        OtpUtil.cleanupExpired();

        List<String> orphanFiles = PendingDoctorStore.cleanupExpiredAndGetOrphanFiles();
        for (String path: orphanFiles) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (IOException e) {
                log.warn("Không thể xoá file chứng chỉ mồ côi: {}", path, e);
            }
        }
        if (!orphanFiles.isEmpty()) {
            log.info("Đã dọn các file mồ côi thành công.");
        }
    }
}
