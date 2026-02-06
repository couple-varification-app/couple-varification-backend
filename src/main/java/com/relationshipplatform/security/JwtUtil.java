package com.relationshipplatform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.query.KeysetScrollDelegate;
import org.springframework.stereotype.Component;

import com.relationshipplatform.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Utility class for token generation and validation
 * Handles all JWT operations
 */
@Component
public class JwtUtil {

    // Get from application.properties
    @Value("${jwt.secret:mySecretKeyForRelationshipPlatformThatIsAtLeast256BitsLong12345678}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // /**
    //  * Generate JWT token for user
    //  */
    // public String generateToken(String userId, String email) {
    //     Map<String, Object> claims = new HashMap<>();
    //     claims.put("userId", userId);
    //     claims.put("email", email);
    //     return createToken(claims, email);
    // }

     //generate token:
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles;

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiration)))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("typ", "access")
                .signWith(secret, SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .addClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract issued at date from token
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return  Jwts.builder()
                .signWith(getSigningKey()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }

    /**
     * Get expiration time in seconds
     */
    public Long getExpirationInSeconds() {
        return expiration / 1000;
    }
}