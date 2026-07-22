package com.mycompany.jpademo.backend.cache;

import com.mycompany.jpademo.backend.dto.request.RegisterRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PendingRegistrationStore {

    private static final Map<String, PendingRegistrationData> store = new ConcurrentHashMap<>();
    private static final int EXPIRATION_MINUTES = 10;

    public static void savePending(String userName, RegisterRequest request) {
        PendingRegistrationData data = PendingRegistrationData.builder()
                .request(request)
                .expireTime(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();
        store.put(userName, data);
    }

    public static RegisterRequest getPending(String userName) {
        PendingRegistrationData data = store.get(userName);
        if (data == null) {
            return null;
        }
        if (LocalDateTime.now().isAfter(data.getExpireTime())) {
            store.remove(userName);
            return null;
        }
        return data.getRequest();
    }

    public static void removePending(String userName) {
        store.remove(userName);
    }

    public static void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        store.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpireTime()));
    }
}
