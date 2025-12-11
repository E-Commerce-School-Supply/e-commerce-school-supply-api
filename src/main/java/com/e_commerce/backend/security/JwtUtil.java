package com.e_commerce.backend.security;

import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        // Try to load the secret from properties. If missing, generate a dev key.
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            // Development fallback: generate a random 256-bit (32-byte) HMAC key (not for production)
            java.security.SecureRandom random = new java.security.SecureRandom();
            byte[] generated = new byte[32];
            random.nextBytes(generated);
            this.key = Keys.hmacShaKeyFor(generated);
        } else {
            // Decodes the secret from Base64 (expected provided value is Base64)
            this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        }
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExp());
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        // Use parserBuilder with the signing key to parse JWS and return claims
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}