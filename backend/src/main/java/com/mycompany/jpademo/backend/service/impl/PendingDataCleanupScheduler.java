package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.cache.PendingStaffStore;
import com.mycompany.jpademo.backend.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        Path root = Paths.get(System.getProperty("user.dir"));
        Path uploadDir;
        if (root.getFileName().toString().equals("backend")) {
            uploadDir = root.resolve(Paths.get("src", "main", "resources", "static", "images", "certificate"));
        } else {
            uploadDir = root.resolve(Paths.get("backend", "src", "main", "resources", "static", "images", "certificate"));
        }

        List<String> orphanFiles = PendingStaffStore.cleanupExpiredAndGetOrphanFiles();
        for (String filename : orphanFiles) {
            try {
                Path filePath = uploadDir.resolve(filename).normalize();
                if (filePath.startsWith(uploadDir)) {
                    Files.deleteIfExists(filePath);
                }
            } catch (IOException e) {
                log.warn("Không thể xoá file chứng chỉ mồ côi: {}", filename, e);
            }
        }
        if (!orphanFiles.isEmpty()) {
            log.info("Đã dọn các file mồ côi thành công.");
        }
    }
}
