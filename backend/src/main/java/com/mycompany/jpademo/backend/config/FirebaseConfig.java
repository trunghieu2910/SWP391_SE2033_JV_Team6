package com.mycompany.jpademo.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@org.springframework.context.annotation.Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized, skipping initialization.");
            return;
        }

        // First, allow overriding the credentials by environment variable (recommended for prod):
        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (envPath != null && !envPath.isBlank()) {
            log.info("Initializing Firebase using GOOGLE_APPLICATION_CREDENTIALS: {}", envPath);
            try (InputStream serviceAccount = new FileInputStream(envPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized from file: {}", envPath);
                return;
            }
        }

        // Fallback to classpath resource (for local dev):
        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
        if (!resource.exists()) {
            // Do not throw raw FileNotFoundException here; log clear message and skip initialization.
            log.warn("Firebase service account not found. Skipping Firebase initialization. " +
                    "To enable Firebase, set GOOGLE_APPLICATION_CREDENTIALS env var or add firebase-service-account.json to classpath (src/main/resources).");
            return;
        }

        log.info("Initializing Firebase using classpath resource firebase-service-account.json");
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized from classpath resource");
        }
    }
}
