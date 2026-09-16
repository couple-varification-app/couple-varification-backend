package com.relationshipplatform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

/**
 * JWT Utility class for token generation and validation
 * Handles all JWT operations
 */
@Component
public class JwtService {
    //  Using for : Development-friendly with secure defaults
    // Get from application.properties
    @Value("${jwt.secret:mySecretKeyForRelationshipPlatformThatIsAtLeast256BitsLong12345678}")
    private String secret;

    @Value("${jwt.expiration:8640000}") // 24 hours in milliseconds
    private Long expiration;

    // LONG-LIVED: Refresh Token (7 days)
    @Value("${jwt.refresh-token.expiration:604800000}") // 7 days
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        //  Added StandardCharsets.UTF_8 for consistency
    }

     /**
     *  BEST PRACTICE: Generate token with minimal claims
     * 
     * @param userId Public user ID (USR-xxx)
     * @param email User's email
     * @return JWT token
     */
    // Generate ACCESS TOKEN (short-lived)
    public String generateToken(String userId, String email) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("type", "ACCESS")           //Token type
                .subject(email)                    //  Modern: subject() instead of setSubject()
                .issuedAt(new Date())              //  Modern: issuedAt() instead of setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration))            //  Modern: expiration() instead of setExpiration()
                .signWith(getSigningKey())         //  Modern: signWith(Key) - algorithm auto-detected
                .compact();
    }

    /**
     * Generate REFRESH TOKEN (long-lived)
     * Note: This is just the JWT. You still need to store it in DB!
     */
    public String generateRefreshToken(String userId, String email){
        
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("type", "REFRESH")  //Token type
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
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
     * Extract token type (ACCESS or REFRESH)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
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
        return Jwts.parser()                       // ✅ Modern: parser() instead of parserBuilder()
                .verifyWith(getSigningKey())       // ✅ Modern: verifyWith() instead of setSigningKey()
                .build()
                .parseSignedClaims(token)          // ✅ Modern: parseSignedClaims() instead of parseClaimsJws()
                .getPayload();                     // ✅ Modern: getPayload() instead of getBody()
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
     * Check if token is an access token
     */
    public Boolean isAccessToken(String token) {
        return "ACCESS".equals(extractTokenType(token));
    }

    /**
     * Check if token is a refresh token
     */
    public Boolean isRefreshToken(String token) {
        return "REFRESH".equals(extractTokenType(token));
    }

    /**
     * Get access token expiration in seconds
     */
    public Long getAccessTokenExpirationInSeconds() {
        return expiration / 1000;
    }

    /**
     * Get refresh token expiration in seconds
     */
    public Long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

    //STILL THE REFRESH TOKEN IS NOT FULLY IMPEMENTED
}