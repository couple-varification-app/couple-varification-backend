package com.relationshipplatform.service;

import com.relationshipplatform.dto.request.restriction.RestrictionApprovalRequestDto;
import com.relationshipplatform.dto.request.restriction.RestrictionCreateRequestDto;
import com.relationshipplatform.dto.request.restriction.RestrictionUpdateRequestDto;
import com.relationshipplatform.dto.response.restriction.RestrictionResponse;

import java.util.List;

/**
 * Service interface for Restriction operations
 * Time-bound restrictions with auto-expiry and renewal
 */
public interface RestrictionService {

    /**
     * Create a new restriction
     * - Requires mutual approval (like Promise)
     * - Must have start date and end date (time-bound)
     * - Validates couple exists and is ACTIVE
     * - Validates couple PIN
     * - Generates unique Restriction ID
     * - Sets status to PENDING (awaiting partner approval)
     * - Creator is auto-approved
     * 
     * @param initiatorUserId User creating the restriction
     * @param request Restriction creation details
     * @return Created restriction with PENDING status
     */
    RestrictionResponse createRestriction(String initiatorUserId, RestrictionCreateRequestDto request);

    /**
     * Partner approves or rejects a restriction
     * - Validates restriction exists and is PENDING
     * - Validates couple PIN
     * - If approved: status changes to ACTIVE
     * - If rejected: restriction is deleted
     * 
     * @param partnerUserId User approving/rejecting
     * @param request Approval details
     * @return Approved restriction or null if rejected
     */
    RestrictionResponse approveRestriction(String partnerUserId, RestrictionApprovalRequestDto request);

    /**
     * Get restriction by ID
     * 
     * @param restrictionId Public restriction ID
     * @return Restriction details
     */
    RestrictionResponse getRestrictionById(String restrictionId);

    /**
     * Get all restrictions for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of all restrictions
     */
    List<RestrictionResponse> getRestrictionsByCouple(String coupleId);

    /**
     * Get active restrictions for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of active restrictions
     */
    List<RestrictionResponse> getActiveRestrictionsByCouple(String coupleId);

    /**
     * Get pending restrictions for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of pending restrictions
     */
    List<RestrictionResponse> getPendingRestrictionsByCouple(String coupleId);

    /**
     * Get restrictions expiring soon (within 7 days)
     * 
     * @param coupleId Public couple ID
     * @return List of expiring restrictions
     */
    List<RestrictionResponse> getExpiringSoonRestrictions(String coupleId);

    /**
     * Update restriction (extend end date, modify)
     * - Requires mutual consent
     * - Can extend end date
     * - Can modify description
     * 
     * @param request Update details
     * @return Updated restriction
     */
    RestrictionResponse updateRestriction(RestrictionUpdateRequestDto request);

    /**
     * Revoke a restriction early (before end date)
     * - Requires mutual consent
     * - Status changes to REVOKED
     * 
     * @param restrictionId Public restriction ID
     * @param couplePin Couple PIN for verification
     * @return Revoked restriction
     */
    RestrictionResponse revokeRestriction(String restrictionId, String couplePin);

    /**
     * Delete a restriction
     * - Only PENDING restrictions can be deleted
     * 
     * @param restrictionId Public restriction ID
     * @param couplePin Couple PIN for verification
     */
    void deleteRestriction(String restrictionId, String couplePin);

    /**
     * Get restrictions by status
     * 
     * @param coupleId Public couple ID
     * @param status Status to filter by
     * @return List of restrictions
     */
    List<RestrictionResponse> getRestrictionsByStatus(String coupleId, String status);

    /**
     * Auto-expire restrictions past their end date
     * Called by scheduler daily
     * 
     * @return Number of restrictions expired
     */
    int expireOverdueRestrictions();

    /**
     * Get restrictions by severity
     * 
     * @param coupleId Public couple ID
     * @param severity Severity level (LIGHT, MODERATE, STRICT)
     * @return List of restrictions
     */
    List<RestrictionResponse> getRestrictionsBySeverity(String coupleId, String severity);
}