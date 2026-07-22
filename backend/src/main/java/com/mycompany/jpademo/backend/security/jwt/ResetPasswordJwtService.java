package com.mycompany.jpademo.backend.security.jwt;

import com.mycompany.jpademo.backend.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Issues and validates short-lived, single-purpose JWTs used to authorize
 * exactly one password reset after a successful OTP verification. Kept
 * separate from the main authentication JWT service (if any) because this
 * token is never used for session/authorization purposes — only to carry
 * "this email proved OTP ownership a moment ago" between step 2 and step 3
 * of the forgot-password flow.
 */
@Service
public class ResetPasswordJwtService {

    /** Base64-encoded HMAC signing secret, distinct from the app's main JWT secret. */
    @Value("${jwt.reset.secret}")
    private String SECRET_KEY;

    /** Token time-to-live, in milliseconds (kept short — see application.yml). */
    @Value("${jwt.reset.expiration}")
    private long EXPIRATION; // 5 minutes in milliseconds

    /** Builds the HMAC signing key from {@link #SECRET_KEY}. */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a reset token for {@code user}, binding it to their email
     * (subject) and their password hash at generation time (claim
     * {@code oldHash}) — the hash binding is what lets
     * {@link com.mycompany.jpademo.backend.service.impl.ForgotPasswordServiceImpl#resetPassword}
     * detect and reject a token that was already used once.
     */
    public String generateResetToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "RESET_PASSWORD")
                .claim("oldHash", user.getPasswordHash())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignKey())
                .compact();
    }

    /** Extracts the email (subject claim) from a reset token. Assumes the token is already known to be valid. */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Extracts the {@code oldHash} claim (the password hash at token-issue time). Assumes the token is already known to be valid. */
    public String extractOldHash(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("oldHash").toString();
    }

    /**
     * Checks whether {@code token} has a valid signature and has not
     * expired.
     *
     * @return true if the token can be parsed and verified successfully;
     *         false for any parsing/verification failure (including expiry)
     */
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
