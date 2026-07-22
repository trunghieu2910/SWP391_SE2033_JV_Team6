package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Outcome of the "resolve Google session" business step (before an actual
 * HTTP session is established). Kept independent of
 * HttpServletRequest/Response so the service layer stays free of any
 * servlet-API dependency.
 */
@Getter
@AllArgsConstructor
public class GoogleSessionResult {

    /** Possible outcomes of a Google sign-in attempt. */
    public enum Status { OK, NEED_MORE_INFO, BANNED, INACTIVE, LOCKED }

    /** The outcome of this sign-in attempt. */
    private final Status status;

    /** Populated only when {@code status == OK}: the existing, authenticated user. */
    private final User user;

    /** Populated only when {@code status == NEED_MORE_INFO}: the verified Google email. */
    private final String email;

    /** Populated only when {@code status == NEED_MORE_INFO}: the Google display name. */
    private final String fullName;

    /** Builds a successful result for an existing, allowed-to-login account. */
    public static GoogleSessionResult ok(User user) {
        return new GoogleSessionResult(Status.OK, user, null, null);
    }

    /** Builds a result indicating this Google email has no account yet, and
     *  the frontend must collect the remaining registration fields. */
    public static GoogleSessionResult needMoreInfo(String email, String fullName) {
        return new GoogleSessionResult(Status.NEED_MORE_INFO, null, email, fullName);
    }

    /** Builds a rejection result for an existing account that is not
     *  currently allowed to log in (BANNED, INACTIVE, or LOCKED). */
    public static GoogleSessionResult rejected(Status status) {
        return new GoogleSessionResult(status, null, null, null);
    }
}