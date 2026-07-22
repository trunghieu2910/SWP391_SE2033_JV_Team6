package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.dto.request.GoogleCompleteRequest;
import com.mycompany.jpademo.backend.dto.response.GoogleSessionResult;
import com.mycompany.jpademo.backend.entity.User;

/**
 * Business logic for signing in with Google, covering both returning users
 * and brand-new (first-time) sign-ups.
 */
public interface GoogleAuthService {

    /**
     * Verifies the given Firebase ID token, looks up the matching user by
     * email, and validates the account's status.
     *
     * @param idToken Firebase ID token obtained from the client-side Google sign-in
     * @return a {@link GoogleSessionResult} describing whether the user can
     *         log in, needs to complete registration, or is rejected
     */
    GoogleSessionResult resolveSession(String idToken);

    /**
     * Creates a new Patient account from the extra profile information
     * collected after a user's very first Google sign-in.
     *
     * @param request Firebase ID token plus the additional required fields
     *                (username, phone number, national ID)
     * @return the newly created, persisted {@link User}
     */
    User completeRegistration(GoogleCompleteRequest request);
}