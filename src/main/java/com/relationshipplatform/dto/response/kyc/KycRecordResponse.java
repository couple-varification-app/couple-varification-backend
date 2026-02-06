package com.relationshipplatform.dto.response.kyc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for KYC Record entity
 * Annual relationship verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycRecordResponse {

    private Long id;
    private String coupleId;
    private Integer year; // Anniversary year (1, 2, 3...)
    private boolean user1Confirmed;
    private boolean user2Confirmed;
    private String status; // VERIFIED, PENDING, EXPIRED
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // When this KYC expires
    
    // User info
    private String user1Name;
    private String user2Name;
    
    // Computed fields
    private boolean fullyVerified; // true if both confirmed
    private boolean isPending; // true if waiting for confirmation
    private boolean isExpired; // true if past expiration
    private Long daysUntilExpiration;
    private String relationshipDuration; // "2 years 3 months"
}