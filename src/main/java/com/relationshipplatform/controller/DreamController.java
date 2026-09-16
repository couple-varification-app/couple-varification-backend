package com.relationshipplatform.controller;

import com.relationshipplatform.dto.request.dream.DreamCreateRequestDto;
import com.relationshipplatform.dto.response.api.ApiResponse;
import com.relationshipplatform.dto.response.dream.DreamResponse;
import com.relationshipplatform.service.DreamService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Dream operations
 * Base URL: /api/dreams
 */
@Slf4j
@RestController
@RequestMapping("/api/dreams")
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
public class DreamController {

    private final DreamService dreamService;

    public DreamController(DreamService dreamService) {
        this.dreamService = dreamService;
    }

    /**
     * Create a new dream
     * POST /api/dreams
     * 
     * Header: X-User-Id (creator's user ID)
     * 
     * Request Body:
     * {
     *   "coupleId": "CPL-xxx",
     *   "description": "Travel to Japan together",
     *   "couplePin": "123456",
     *   "category": "TRAVEL",
     *   "targetDate": "2027-12-31"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DreamResponse>> createDream(
            @RequestHeader("X-User-Id") String creatorUserId,
            @Valid @RequestBody DreamCreateRequestDto request) {
        
        log.info("POST /api/dreams - Creating dream for couple: {}", request.getCoupleId());
        
        DreamResponse dream = dreamService.createDream(creatorUserId, request);
        ApiResponse<DreamResponse> response = ApiResponse.success(
            dream,
            "Dream created successfully"
        );
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get dream by ID
     * GET /api/dreams/{dreamId}
     */
    @GetMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<DreamResponse>> getDreamById(
            @PathVariable String dreamId) {
        
        log.info("GET /api/dreams/{} - Fetching dream", dreamId);
        
        DreamResponse dream = dreamService.getDreamById(dreamId);
        ApiResponse<DreamResponse> response = ApiResponse.success(
            dream,
            "Dream retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all dreams for a couple
     * GET /api/dreams/couple/{coupleId}
     */
    @GetMapping("/couple/{coupleId}")
    public ResponseEntity<ApiResponse<List<DreamResponse>>> getDreamsByCouple(
            @PathVariable String coupleId) {
        
        log.info("GET /api/dreams/couple/{} - Fetching all dreams", coupleId);
        
        List<DreamResponse> dreams = dreamService.getDreamsByCouple(coupleId);
        ApiResponse<List<DreamResponse>> response = ApiResponse.success(
            dreams,
            String.format("%d dream(s) retrieved", dreams.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get active dreams for a couple
     * GET /api/dreams/couple/{coupleId}/active
     */
    @GetMapping("/couple/{coupleId}/active")
    public ResponseEntity<ApiResponse<List<DreamResponse>>> getActiveDreams(
            @PathVariable String coupleId) {
        
        log.info("GET /api/dreams/couple/{}/active - Fetching active dreams", coupleId);
        
        List<DreamResponse> dreams = dreamService.getActiveDreamsByCouple(coupleId);
        ApiResponse<List<DreamResponse>> response = ApiResponse.success(
            dreams,
            String.format("%d active dream(s)", dreams.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get achieved dreams for a couple
     * GET /api/dreams/couple/{coupleId}/achieved
     */
    @GetMapping("/couple/{coupleId}/achieved")
    public ResponseEntity<ApiResponse<List<DreamResponse>>> getAchievedDreams(
            @PathVariable String coupleId) {
        
        log.info("GET /api/dreams/couple/{}/achieved - Fetching achieved dreams", coupleId);
        
        List<DreamResponse> dreams = dreamService.getAchievedDreamsByCouple(coupleId);
        ApiResponse<List<DreamResponse>> response = ApiResponse.success(
            dreams,
            String.format("%d achieved dream(s) 🎉", dreams.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get dreams by status
     * GET /api/dreams/couple/{coupleId}/status/{status}
     * 
     * Status values: ACTIVE, ACHIEVED, ABANDONED
     */
    @GetMapping("/couple/{coupleId}/status/{status}")
    public ResponseEntity<ApiResponse<List<DreamResponse>>> getDreamsByStatus(
            @PathVariable String coupleId,
            @PathVariable String status) {
        
        log.info("GET /api/dreams/couple/{}/status/{}", coupleId, status);
        
        List<DreamResponse> dreams = dreamService.getDreamsByStatus(coupleId, status);
        ApiResponse<List<DreamResponse>> response = ApiResponse.success(
            dreams,
            String.format("%d %s dream(s)", dreams.size(), status.toLowerCase())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get dreams by category
     * GET /api/dreams/couple/{coupleId}/category/{category}
     * 
     * Categories: TRAVEL, FINANCIAL, FAMILY, CAREER, HEALTH, HOME, etc.
     */
    @GetMapping("/couple/{coupleId}/category/{category}")
    public ResponseEntity<ApiResponse<List<DreamResponse>>> getDreamsByCategory(
            @PathVariable String coupleId,
            @PathVariable String category) {
        
        log.info("GET /api/dreams/couple/{}/category/{}", coupleId, category);
        
        List<DreamResponse> dreams = dreamService.getDreamsByCategory(coupleId, category);
        ApiResponse<List<DreamResponse>> response = ApiResponse.success(
            dreams,
            String.format("%d %s dream(s)", dreams.size(), category.toLowerCase())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update dream status (mark as ACHIEVED or ABANDONED)
     * PUT /api/dreams/{dreamId}/status
     * 
     * Request Body:
     * {
     *   "dreamId": "DRM-xxx",
     *   "couplePin": "123456",
     *   "newStatus": "ACHIEVED",
     *   "achievementNotes": "We finally did it!",
     *   "achievementDate": "2026-12-25"
     * }
     */
    // @PutMapping("/{dreamId}/status")
    // public ResponseEntity<ApiResponse<DreamResponse>> updateDreamStatus(
    //         @PathVariable String dreamId,
    //         @Valid @RequestBody DreamUpdateRequestDto request) {
        
    //     log.info("PUT /api/dreams/{}/status - New status: {}", dreamId, request.getNewStatus());
        
    //     // Ensure dreamId matches
    //     request.setDreamId(dreamId);
        
    //     DreamResponse dream = dreamService.updateDreamStatus(request);
    //     ApiResponse<DreamResponse> response = ApiResponse.success(
    //         dream,
    //         dream.isAchieved() ? "Dream achieved! 🎉 +5 health bonus" : "Dream status updated"
    //     );
        
    //     return ResponseEntity.ok(response);
    // }

    /**
     * Delete a dream
     * DELETE /api/dreams/{dreamId}
     * 
     * Query Param: pin=123456
     */
    @DeleteMapping("/{dreamId}")
    public ResponseEntity<ApiResponse<Void>> deleteDream(
            @PathVariable String dreamId,
            @RequestParam String pin) {
        
        log.info("DELETE /api/dreams/{} - Deleting dream", dreamId);
        
        dreamService.deleteDream(dreamId, pin);
        ApiResponse<Void> response = ApiResponse.success("Dream deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get dream statistics for a couple
     * GET /api/dreams/couple/{coupleId}/stats
     * 
     * Returns count of achieved dreams
     */
    @GetMapping("/couple/{coupleId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDreamStats(
            @PathVariable String coupleId) {
        
        log.info("GET /api/dreams/couple/{}/stats", coupleId);
        
        long achievedCount = dreamService.getAchievedDreamsCount(coupleId);
        
        Map<String, Object> stats = Map.of(
            "achievedCount", achievedCount,
            "message", achievedCount > 0 
                ? String.format("You've achieved %d dream%s together! 🌟", achievedCount, achievedCount == 1 ? "" : "s")
                : "Start creating dreams together!"
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(
            stats,
            "Dream statistics retrieved"
        );
        
        return ResponseEntity.ok(response);
    }
}