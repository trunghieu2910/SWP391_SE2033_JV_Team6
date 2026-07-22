package com.mycompany.jpademo.backend.service.interfaces;

import com.google.firebase.auth.FirebaseToken;

/**
 * Thin wrapper around the Firebase Admin SDK, used to verify Google ID
 * tokens issued by Firebase Authentication on the client side.
 */
public interface FirebaseService {

    /**
     * Verifies a Firebase ID token's signature, expiration, and revocation
     * status against Google's servers.
     *
     * @param idToken the Firebase ID token obtained by the frontend after a
     *                successful Google sign-in in the browser
     * @return the decoded {@link FirebaseToken}, containing verified claims
     *         such as email and display name
     */
    public FirebaseToken verifyIdToken(String idToken);
}
