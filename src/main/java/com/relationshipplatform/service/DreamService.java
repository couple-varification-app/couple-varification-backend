package com.relationshipplatform.service;

import java.util.List;

import com.relationshipplatform.dto.request.dream.DreamCreateRequestDto;
import com.relationshipplatform.dto.response.dream.DreamResponse;

public interface DreamService {
    /**
     * Create a new dream
     * - No approval needed (both partners can create freely)
     * - Validates couple exists and is ACTIVE
     * - Validates couple PIN
     * - Generates unique Dream ID
     * - Sets status to ACTIVE immediately
     * 
     * @param creatorUserId User creating the dream
     * @param request Dream creation details
     * @return Created dream with ACTIVE status
     */
    DreamResponse createDream(String creatorUserId, DreamCreateRequestDto request);

    /**
     * Get dream by ID
     * 
     * @param dreamId Public dream ID
     * @return Dream details
     */
    DreamResponse getDreamById(String dreamId);

    /**
     * Get all dreams for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of all dreams
     */
    List<DreamResponse> getDreamsByCouple(String coupleId);

    /**
     * Get active dreams for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of active dreams
     */
    List<DreamResponse> getActiveDreamsByCouple(String coupleId);

    /**
     * Get achieved dreams for a couple
     * 
     * @param coupleId Public couple ID
     * @return List of achieved dreams
     */
    List<DreamResponse> getAchievedDreamsByCouple(String coupleId);

    /**
     * Update dream status (mark as ACHIEVED or ABANDONED)
     * - If marked ACHIEVED: increases relationship health by 5
     * - Records achievement date and notes
     * 
     * @param request Update details
     * @return Updated dream
     */
    // DreamResponse updateDreamStatus(DreamUpdateRequestDto request);

    /**
     * Delete a dream
     * - Only ACTIVE dreams can be deleted
     * 
     * @param dreamId Public dream ID
     * @param couplePin Couple PIN for verification
     */
    void deleteDream(String dreamId, String couplePin);

    /**
     * Get dreams by status
     * 
     * @param coupleId Public couple ID
     * @param status Status to filter by (ACTIVE, ACHIEVED, ABANDONED)
     * @return List of dreams with specified status
     */
    List<DreamResponse> getDreamsByStatus(String coupleId, String status);

    /**
     * Get dreams by category
     * 
     * @param coupleId Public couple ID
     * @param category Category to filter by
     * @return List of dreams in category
     */
    List<DreamResponse> getDreamsByCategory(String coupleId, String category);

    /**
     * Get achieved dreams count
     * 
     * @param coupleId Public couple ID
     * @return Count of achieved dreams
     */
    long getAchievedDreamsCount(String coupleId);
}
