package com.relationshipplatform.service;

import java.util.List;

import com.relationshipplatform.dto.request.promise.PromiseApprovalRequestDto;
import com.relationshipplatform.dto.request.promise.PromiseCreateRequestDto;
import com.relationshipplatform.dto.request.promise.PromiseUpdateRequestDto;
import com.relationshipplatform.dto.response.promise.PromiseResponse;

public interface PromiseService {
    /**
     * Create a new promise
     * - Validates couple exists and is ACTIVE
     * - Validates couple PIN
     * - Generates unique Promise ID
     * - Sets status to PENDING (awaiting partner approval)
     * - Creator is auto-approved
     * 
     * @param initiatorUserId User creating the promise
     * @param request Promise creation details
     * @return Created promise with PENDING status
     */
    PromiseResponse createPromise(String initiatorUserId, PromiseCreateRequestDto request);

    /**
     * Partner approves or rejects a promise
     * - Validates promise exists and is PENDING
     * - Validates couple PIN
     * - If approved: status changes to ACTIVE
     * - If rejected: promise is deleted
     * 
     * @param partnerUserId User approving/rejecting
     * @param request Approval details
     * @return Approved promise or null if rejected
     */
    PromiseResponse approvePromise(String partnerUserId, PromiseApprovalRequestDto request);

    /**
     * Get promise by ID
     * 
     * @param promiseId Public promise ID
     * @return Promise details
     */
    PromiseResponse getPromiseById(String promiseId);

    /**
     * Get all promises for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of all promises
     */
    List<PromiseResponse> getPromisesByCouple(String coupleId);

    /**
     * Get active promises for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of active promises
     */
    List<PromiseResponse> getActivePromisesByCouple(String coupleId);

    /**
     * Get pending promises for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of pending promises
     */
    List<PromiseResponse> getPendingPromisesByCouple(String coupleId);

    /**
     * Update promise status (COMPLETED/BROKEN)
     * - Requires mutual consent for status change
     * - If marked BROKEN: reduces relationship health by 10
     * - If marked COMPLETED: increases health by 5
     * 
     * @param request Update details
     * @return Updated promise
     */
    PromiseResponse updatePromiseStatus(PromiseUpdateRequestDto request);

    /**
     * Delete a promise
     * - Requires mutual consent
     * - Only PENDING or ACTIVE promises can be deleted
     * 
     * @param promiseId Public promise ID
     * @param couplePin Couple PIN for verification
     */
    void deletePromise(String promiseId, String couplePin);

    /**
     * Get promises by status for a couple
     * 
     * @param coupleId Public couple ID
     * @param status Status to filter by
     * @return List of promises with specified status
     */
    List<PromiseResponse> getPromisesByStatus(String coupleId, String status);

    /**
     * Get completed promises count for a couple
     * 
     * @param coupleId Public couple ID
     * @return Count of completed promises
     */
    long getCompletedPromisesCount(String coupleId);

    /**
     * Get broken promises count for a couple
     * 
     * @param coupleId Public couple ID
     * @return Count of broken promises
     */
    long getBrokenPromisesCount(String coupleId);
}
