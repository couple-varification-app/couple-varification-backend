package com.relationshipplatform.dto.response.relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Relationship status snapshot for a user.
 * 
 * This DTO represents the current relationship-related state
 * required by client applications. All computed or UI-specific
 * logic must be handled in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipStatusInfo {

    // ========================================
    // ACTIVE RELATIONSHIP
    // ========================================

    /**
     * Whether user is currently in an active relationship
     */
    private boolean active;

    /**
     * Active couple ID (null if not active)
     */
    private String coupleId;

    /**
     * Partner basic info (null if not active)
     */
    private String partnerUserId;
    private String partnerName;

    /**
     * Relationship metrics (null if not active)
     */
    private Integer relationshipHealth;
    private Integer loyaltyScore;

    /**
     * Relationship start date (null if not active)
     */
    private LocalDate relationshipStartDate;

    // ========================================
    // PENDING REQUEST
    // ========================================

    /**
     * Incoming couple request (null if none)
     */
    private String pendingCoupleId;
    private String pendingRequestFromUserId;
    private String pendingRequestFromName;

    // ========================================
    // HISTORY & COOLDOWN
    // ========================================

    /**
     * Number of past relationships
     */
    private Integer pastRelationshipCount;

    /**
     * Cooldown end date after breakup (null if not in cooldown)
     */
    private LocalDate cooldownEndDate;

    // ========================================
    // DASHBOARD METRICS
    // ========================================
    private boolean hasActiveCouple;
    private boolean hasPendingCoupleRequest;
    private String pendingRequestFrom;
    private Integer activePromisesCount;
    private Integer activeDreamsCount;
    private Integer activeRestrictionsCount;

    private Integer pendingApprovalsCount;

    // ========================================
    // KYC
    // ========================================

    /**
     * KYC verification status for current year
     */
    private Boolean kycVerified;

    /**
     * Current anniversary year (null if not active)
     */
    private Integer currentAnniversaryYear;
}
