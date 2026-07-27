package com.mycompany.jpademo.backend.service.impl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mycompany.jpademo.backend.exception.UnauthorizedException;
import com.mycompany.jpademo.backend.service.interfaces.FirebaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link FirebaseService}, backed by the
 * Firebase Admin SDK ({@link FirebaseAuth}).
 */
@Service
public class FirebaseServiceImpl implements FirebaseService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseServiceImpl.class);

    /**
     * {@inheritDoc}
     * <p>
     * Verification is performed with {@code checkRevoked = true}, so a
     * token that is still cryptographically valid but has been revoked
     * (e.g. the user's Google session was revoked, or an admin disabled
     * the Firebase account) will still be rejected.
     *
     * @throws UnauthorizedException if Firebase is not configured on this
     *                                environment, or if the token is
     *                                invalid, expired, or revoked
     */
    public FirebaseToken verifyIdToken(String idToken) {
        if (FirebaseApp.getApps().isEmpty()) {
            String msg = "Firebase is not configured. Please set GOOGLE_APPLICATION_CREDENTIALS or add firebase-service-account.json to classpath.";
            log.error("❌ {}", msg);
            throw new UnauthorizedException(msg);
        }

        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            return firebaseAuth.verifyIdToken(idToken, true);
        } catch (FirebaseAuthException e) {
            // Log detailed error info for debugging
            log.error("❌ Firebase authentication error during ID token verification", e);
            log.error("   Error Code: {}", e.getErrorCode());
            log.error("   Error Message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("   Root Cause: {}", e.getCause().getMessage());
            }
            throw new UnauthorizedException("Invalid Firebase token: " + e.getMessage());
        }
    }
}
