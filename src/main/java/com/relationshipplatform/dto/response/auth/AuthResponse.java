package com.relationshipplatform.dto.response.auth;


import java.time.LocalDateTime;

import com.relationshipplatform.dto.response.user.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication
 * Contains JWT token, user info, and relationship status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // JWT Token
    private String token;
    private String tokenType; // "Bearer"
    private Long expiresIn; // Seconds until expiration
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    
    // User Information
    private UserResponse user;
    
    // Relationship Status (important for app flow)
    private RelationshipStatusInfo relationshipStatus;
    
    // Response metadata
    private String message;
    private boolean firstLogin; // true if user's first login
    
    /**
     * Nested class for relationship status information
     * Helps client decide which screen to show after login
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipStatusInfo {
        private boolean hasActiveCouple;
        private String coupleId; // null if no active couple
        private String partnerName; // null if no active couple
        private Integer relationshipHealth; // null if no active couple
        private boolean hasPendingCoupleRequest; // true if someone sent request
        private String pendingRequestFrom; // User ID who sent request
    }
}