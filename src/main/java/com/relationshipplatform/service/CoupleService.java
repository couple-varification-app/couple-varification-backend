package com.relationshipplatform.service;

import java.util.List;

import com.relationshipplatform.dto.request.couple.CoupleAcceptanceRequest;
import com.relationshipplatform.dto.request.couple.CoupleRegistrationRequest;
import com.relationshipplatform.dto.request.couple.CoupleUpdateRequestDto;
import com.relationshipplatform.dto.response.couple.CoupleResponse;

public interface CoupleService {
    /**
     * Create couple registration request
     * - Validates both users exist and are 18+
     * - Checks neither user is in active couple
     * - Validates and encrypts Couple PIN
     * - Generates unique Couple ID
     * - Sets status to PENDING awaiting partner acceptance
     * 
     * @param initiatorUserId User initiating the couple request
     * @param request Couple registration details
     * @return Created couple response (PENDING status)
     */
    CoupleResponse createCoupleRequest(String initiatorUserId, CoupleRegistrationRequest request);

    /**
     * Partner accepts or rejects couple request
     * - Validates Couple PIN
     * - If accepted: sets status to ACTIVE, initializes health to 100
     * - If rejected: deletes the couple record
     * 
     * @param partnerUserId User accepting/rejecting the request
     * @param request Acceptance details
     * @return Updated couple response or null if rejected
     */
    CoupleResponse respondToCoupleRequest(String partnerUserId, CoupleAcceptanceRequest request);

    /**
     * Get couple details by Couple ID
     * 
     * @param coupleId Public couple ID
     * @return Couple details
     */
    CoupleResponse getCoupleById(String coupleId);

    /**
     * Get active couple for a user
     * 
     * @param userId Public user ID
     * @return Active couple details or null
     */
    CoupleResponse getActiveCoupleByUser(String userId);

    /**
     * Get pending couple request for a user (where user is partner)
     * 
     * @param userId Public user ID
     * @return Pending couple request or null
     */
    CoupleResponse getPendingCoupleRequestForUser(String userId);

    /**
     * Update couple information (PIN change, etc.)
     * 
     * @param request Update details
     * @return Updated couple response
     */
    CoupleResponse updateCouple(CoupleUpdateRequestDto request);

    /**
     * Verify couple PIN
     * Used for secure operations
     * 
     * @param coupleId Public couple ID
     * @param pin Couple PIN to verify
     * @return true if PIN is correct
     */
    boolean verifyCouplePin(String coupleId, String pin);

    /**
     * Update relationship health score
     * 
     * @param coupleId Public couple ID
     * @param newScore New health score (0-100)
     */
    void updateRelationshipHealth(String coupleId, int newScore);

    /**
     * Update loyalty score
     * 
     * @param coupleId Public couple ID
     * @param newScore New loyalty score
     */
    void updateLoyaltyScore(String coupleId, int newScore);

    /**
     * Get all couples (admin only)
     * 
     * @return List of all couples
     */
    List<CoupleResponse> getAllCouples();
}
