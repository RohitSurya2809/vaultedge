package com.rohitsurya2809.vaultedge.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final Key signingKey;
    private final long expirationSeconds;

    public JwtUtil(
            @Value("${jwt.secret:1234567890abcdefghijklmnopqrstuvwxyz}") String secret,
            @Value("${jwt.expiration:3600}") long expirationSeconds) {
        // Ensure secret has enough entropy; decode as bytes and create HMAC key.
        // If secret is base64-encoded you can decode; here we use raw bytes of the string.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Generate JWT with username as subject and userId stored as claim "uid".
     * @param username subject (email)
     * @param userId UUID of the authenticated user (may be null)
     * @param additionalClaims optional other claims map
     * @return signed JWT string
     */
    public String generateToken(String username, UUID userId, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey, SignatureAlgorithm.HS256);

        if (userId != null) {
            builder.claim("uid", userId.toString());
        }

        if (additionalClaims != null && !additionalClaims.isEmpty()) {
            builder.addClaims(additionalClaims);
        }

        return builder.compact();
    }

    public String generateToken(String username, UUID userId) {
        return generateToken(username, userId, null);
    }

    /**
     * Validate token signature and expiration.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // includes ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SecurityException
            return false;
        }
    }

    /**
     * Extract subject (username/email).
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract userId claim (uid) as UUID if present, otherwise null.
     */
    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object uid = claims.get("uid");
        if (uid == null) return null;
        try {
            return UUID.fromString(uid.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Generic claim extractor.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Returns token expiration as epoch millis
     */
    public long getExpirationMillis(String token) {
        Claims claims = extractAllClaims(token);
        Date exp = claims.getExpiration();
        return exp != null ? exp.getTime() : -1;
    }

    /**
     * Convenience: return remaining seconds until expiry (or -1 if missing).
     */
    public long getRemainingSeconds(String token) {
        Claims claims = extractAllClaims(token);
        Date exp = claims.getExpiration();
        if (exp == null) return -1;
        long now = System.currentTimeMillis();
        return Math.max(0, (exp.getTime() - now) / 1000);
    }
}
