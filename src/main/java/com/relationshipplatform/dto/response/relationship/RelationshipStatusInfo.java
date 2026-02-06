package com.relationshipplatform.dto.response.relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Relationship Status Information
 * 
 * Provides comprehensive relationship state for a user
 * Used in authentication responses, dashboard, and user profile
 * 
 * This helps the client application understand the user's current
 * relationship state and navigate accordingly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipStatusInfo {

    // ========================================
    // ACTIVE COUPLE INFORMATION
    // ========================================
    
    /**
     * Whether user is currently in an active couple
     * true = user has an active relationship
     * false = user is single or has pending/broken relationship
     */
    private boolean hasActiveCouple;
    
    /**
     * Public Couple ID if user has active couple
     * null if hasActiveCouple = false
     */
    private String coupleId;
    
    /**
     * Partner's name if user has active couple
     * null if hasActiveCouple = false
     */
    private String partnerName;
    
    /**
     * Partner's User ID if user has active couple
     * null if hasActiveCouple = false
     */
    private String partnerUserId;
    
    /**
     * Current relationship health score (0-100)
     * null if hasActiveCouple = false
     */
    private Integer relationshipHealth;
    
    /**
     * Health status category
     * Values: EXCELLENT, GOOD, FAIR, POOR, CRITICAL
     * null if hasActiveCouple = false
     */
    private String healthStatus;
    
    /**
     * Current loyalty score
     * null if hasActiveCouple = false
     */
    private Integer loyaltyScore;
    
    /**
     * Date when relationship started
     * null if hasActiveCouple = false
     */
    private LocalDate relationshipStartDate;
    
    /**
     * Human-readable relationship duration
     * Example: "2 years 3 months"
     * null if hasActiveCouple = false
     */
    private String relationshipDuration;
    
    // ========================================
    // PENDING COUPLE REQUEST INFORMATION
    // ========================================
    
    /**
     * Whether user has pending couple request(s)
     * true = someone sent a couple request to this user
     * false = no pending requests
     */
    private boolean hasPendingCoupleRequest;
    
    /**
     * User ID of person who sent the couple request
     * null if hasPendingCoupleRequest = false
     * Note: Currently supports one pending request at a time
     */
    private String pendingRequestFrom;
    
    /**
     * Name of person who sent the couple request
     * null if hasPendingCoupleRequest = false
     */
    private String pendingRequestFromName;
    
    /**
     * Couple ID of the pending request
     * null if hasPendingCoupleRequest = false
     */
    private String pendingCoupleId;
    
    // ========================================
    // PAST RELATIONSHIP INFORMATION
    // ========================================
    
    /**
     * Whether user has any broken relationships
     * true = user has history of breakup(s)
     */
    private boolean hasPastRelationship;
    
    /**
     * Number of past relationships
     */
    private Integer pastRelationshipCount;
    
    /**
     * Whether user is in cooldown period after breakup
     * true = within 6 months of breakup, cannot create new couple
     */
    private boolean inCooldownPeriod;
    
    /**
     * Days remaining in cooldown period
     * null if not in cooldown
     */
    private Long cooldownDaysRemaining;
    
    // ========================================
    // RELATIONSHIP METRICS (for dashboard)
    // ========================================
    
    /**
     * Total number of active promises
     * 0 if no active couple
     */
    private Integer activePromisesCount;
    
    /**
     * Total number of active dreams
     * 0 if no active couple
     */
    private Integer activeDreamsCount;
    
    /**
     * Total number of active restrictions
     * 0 if no active couple
     */
    private Integer activeRestrictionsCount;
    
    /**
     * Whether user has any pending approvals
     * (promises/restrictions awaiting user's approval)
     */
    private boolean hasPendingApprovals;
    
    /**
     * Count of items pending user's approval
     */
    private Integer pendingApprovalsCount;
    
    // ========================================
    // KYC STATUS
    // ========================================
    
    /**
     * Whether current year's KYC is verified
     * null if no active couple
     */
    private Boolean kycVerified;
    
    /**
     * Current anniversary year
     * null if no active couple
     */
    private Integer currentAnniversaryYear;
    
    // ========================================
    // COMPUTED PROPERTIES
    // ========================================
    
    /**
     * User's overall relationship state for UI routing
     * Values: 
     * - SINGLE: No active couple, no pending request, not in cooldown
     * - PENDING: Has incoming couple request
     * - ACTIVE: In active relationship
     * - COOLDOWN: Post-breakup, cannot create couple yet
     */
    private String overallStatus;
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Check if user can create a new couple request
     * @return true if user is single and not in cooldown
     */
    public boolean canCreateCoupleRequest() {
        return !hasActiveCouple && !hasPendingCoupleRequest && !inCooldownPeriod;
    }
    
    /**
     * Check if user needs to take action
     * @return true if has pending approvals or pending couple request
     */
    public boolean needsAction() {
        return hasPendingCoupleRequest || (hasPendingApprovals != null && hasPendingApprovals);
    }
    
    /**
     * Get user's current state as simple string
     * @return "SINGLE", "PENDING", "ACTIVE", or "COOLDOWN"
     */
    public String getSimpleStatus() {
        if (hasActiveCouple) return "ACTIVE";
        if (hasPendingCoupleRequest) return "PENDING";
        if (inCooldownPeriod) return "COOLDOWN";
        return "SINGLE";
    }
}