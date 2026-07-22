package com.mycompany.jpademo.backend.service.impl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mycompany.jpademo.backend.exception.UnauthorizedException;
import com.mycompany.jpademo.backend.service.interfaces.FirebaseService;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link FirebaseService}, backed by the
 * Firebase Admin SDK ({@link FirebaseAuth}).
 */
@Service
public class FirebaseServiceImpl implements FirebaseService {

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
            throw new UnauthorizedException("Firebase is not configured. Please set GOOGLE_APPLICATION_CREDENTIALS or add firebase-service-account.json to classpath.");
        }

        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            return firebaseAuth.verifyIdToken(idToken, true);
        } catch (FirebaseAuthException e) {
            throw new UnauthorizedException("Invalid Firebase token");
        }
    }
}
