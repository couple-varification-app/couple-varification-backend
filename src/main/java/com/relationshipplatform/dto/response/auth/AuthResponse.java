package com.relationshipplatform.dto.response.auth;


import java.time.LocalDateTime;

import com.relationshipplatform.dto.response.relationship.RelationshipStatusInfo;
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
    
}