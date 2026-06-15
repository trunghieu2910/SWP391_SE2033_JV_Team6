package com.mycompany.jpademo.backend.security.jwt;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.sql.Timestamp;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Date extractIssuedAt(String token) {
        return extractClaims(token).getIssuedAt();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        Date issuedAt = extractIssuedAt(token);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();

        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && (user.getLastChangePassTime() == null
                || issuedAt.after(Timestamp.valueOf(user.getLastChangePassTime())))
                && (user.getLastLogoutTime() == null
                || issuedAt.after(Timestamp.valueOf(user.getLastLogoutTime())))
                ;
    }
}
