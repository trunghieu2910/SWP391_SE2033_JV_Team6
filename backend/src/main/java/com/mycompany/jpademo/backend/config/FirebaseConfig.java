package com.mycompany.jpademo.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@org.springframework.context.annotation.Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void initFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized, skipping initialization.");
            return;
        }

        // First, allow overriding the credentials by environment variable (recommended for prod):
        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (envPath != null && !envPath.isBlank()) {
            log.info("🔧 Initializing Firebase using GOOGLE_APPLICATION_CREDENTIALS environment variable");
            log.info("   Path: {}", envPath);
            try (InputStream serviceAccount = new FileInputStream(envPath)) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                logCredentialsInfo(credentials, envPath);
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized successfully from environment variable path");
                return;
            } catch (Exception e) {
                log.error("❌ Failed to initialize Firebase from environment variable path: {}", envPath, e);
                throw e;
            }
        }

        log.info("⚠️  GOOGLE_APPLICATION_CREDENTIALS environment variable not set, falling back to classpath resource");

        // Fallback to classpath resource (for local dev):
        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
        if (!resource.exists()) {
            // Do not throw raw FileNotFoundException here; log clear message and skip initialization.
            log.warn("❌ Firebase service account not found. Skipping Firebase initialization. " +
                    "To enable Firebase, set GOOGLE_APPLICATION_CREDENTIALS env var or add firebase-service-account.json to classpath (src/main/resources).");
            return;
        }

        log.info("🔧 Initializing Firebase using classpath resource: firebase-service-account.json");
        try (InputStream serviceAccount = resource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            logCredentialsInfo(credentials, "classpath:firebase-service-account.json");
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
            FirebaseApp.initializeApp(options);
            log.info("✅ Firebase initialized successfully from classpath resource");
        } catch (Exception e) {
            log.error("❌ Failed to initialize Firebase from classpath resource", e);
            throw e;
        }
    }

    /**
     * Log non-sensitive credentials metadata (private_key_id, client_email).
     * This helps identify which service account is being used without exposing the private key.
     */
    private void logCredentialsInfo(GoogleCredentials credentials, String source) {
        try {
            InputStream is = null;
            if (source.startsWith("classpath:")) {
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                is = resource.getInputStream();
            } else {
                is = new FileInputStream(source);
            }
            
            if (is != null) {
                JsonNode json = mapper.readTree(is);
                String privateKeyId = json.get("private_key_id").asText();
                String clientEmail = json.get("client_email").asText();
                log.info("   Private Key ID: {}", privateKeyId);
                log.info("   Client Email: {}", clientEmail);
                is.close();
            }
        } catch (Exception e) {
            log.debug("Could not extract credentials metadata from JSON file", e);
        }
    }
}
