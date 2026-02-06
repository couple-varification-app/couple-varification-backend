package com.relationshipplatform.dto.response.restriction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Restriction entity
 * Time-bound restrictions with mutual consent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionResponse {

    private String restrictionId;
    private String coupleId;
    private String description;
    private boolean user1Approved;
    private boolean user2Approved;
    private String status; // ACTIVE, EXPIRED, REVOKED, PENDING
    
    // Time-bound fields (from improvements document)
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Optional: Include basic couple info
    private String user1Name;
    private String user2Name;
    
    // Computed fields
    private boolean fullyApproved; // true if both users approved
    private boolean isPending; // true if waiting for approval
    private boolean isExpired; // true if past end date
    private Long daysRemaining; // Days until expiration
}