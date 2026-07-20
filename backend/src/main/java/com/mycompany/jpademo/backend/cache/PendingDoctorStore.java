package com.mycompany.jpademo.backend.cache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

public class PendingDoctorStore {
    private static final Map<String, PendingDoctorData> STORE = new ConcurrentHashMap<>();
    private static final int PENDING_EXPIRE_MINUTES = 10;

    public static List<String> cleanupExpiredAndGetOrphanFiles() {
        LocalDateTime now = LocalDateTime.now();
        java.util.List<String> orphanFiles = new java.util.ArrayList<>();
        STORE.entrySet().removeIf(e -> {
            boolean expired = now.isAfter(e.getValue().getExpireAt());
            if (expired && e.getValue().getCertificateUrl() != null) {
                orphanFiles.add(e.getValue().getCertificateUrl());
            }
            return expired;
        });
        return orphanFiles;
    }

    public static String savePending(String adminEmail, PendingDoctorData data) {
        String id = data.getRequestId();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            data.setRequestId(id);
        }
        data.setAdminEmail(adminEmail);
        data.setCreatedAt(LocalDateTime.now());
        data.setExpireAt(LocalDateTime.now().plusMinutes(PENDING_EXPIRE_MINUTES));
        STORE.put(id, data);
        return id;
    }

    public static PendingDoctorData getPending(String requestId) {
        PendingDoctorData data = STORE.get(requestId);
        if (data == null) return null;
        if (LocalDateTime.now().isAfter(data.getExpireAt())) {
            STORE.remove(requestId);
            return null;
        }
        return data;
    }

    public static PendingDoctorData getPendingByAdminEmail(String adminEmail) {
        Optional<PendingDoctorData> opt = STORE.values().stream()
                .filter(d -> adminEmail.equals(d.getAdminEmail()))
                .filter(d -> LocalDateTime.now().isBefore(d.getExpireAt()))
                .findFirst();
        if (opt.isPresent()) return opt.get();
        return null;
    }

    public static void removePending(String requestId) {
        STORE.remove(requestId);
    }

    public static void removePendingByAdminEmail(String adminEmail) {
        STORE.entrySet().removeIf(e -> adminEmail.equals(e.getValue().getAdminEmail()));
    }
}