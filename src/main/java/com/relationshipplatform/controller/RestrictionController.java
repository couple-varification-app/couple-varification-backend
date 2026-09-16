package com.relationshipplatform.controller;

import com.relationshipplatform.dto.request.restriction.RestrictionApprovalRequestDto;
import com.relationshipplatform.dto.request.restriction.RestrictionCreateRequestDto;
import com.relationshipplatform.dto.request.restriction.RestrictionUpdateRequestDto;
import com.relationshipplatform.dto.response.api.ApiResponse;
import com.relationshipplatform.dto.response.restriction.RestrictionResponse;
import com.relationshipplatform.service.RestrictionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Restriction operations
 * Base URL: /api/restrictions
 */
@Slf4j
@RestController
@RequestMapping("/api/restrictions")
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
public class RestrictionController {

    private final RestrictionService restrictionService;

    public RestrictionController(RestrictionService restrictionService) {
        this.restrictionService = restrictionService;
    }

    /**
     * Create a new restriction
     * POST /api/restrictions
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RestrictionResponse>> createRestriction(
            @RequestHeader("X-User-Id") String initiatorUserId,
            @Valid @RequestBody RestrictionCreateRequestDto request) {
        
        log.info("POST /api/restrictions - Creating restriction for couple: {}", request.getCoupleId());
        
        RestrictionResponse restriction = restrictionService.createRestriction(initiatorUserId, request);
        ApiResponse<RestrictionResponse> response = ApiResponse.success(
            restriction,
            "Restriction created. Awaiting partner approval."
        );
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Approve or reject a restriction
     * POST /api/restrictions/approve
     */
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<RestrictionResponse>> approveRestriction(
            @RequestHeader("X-User-Id") String partnerUserId,
            @Valid @RequestBody RestrictionApprovalRequestDto request) {
        
        log.info("POST /api/restrictions/approve - Partner: {}, Restriction: {}, Approved: {}",
                 partnerUserId, request.getRestrictionId(), request.getApproved());
        
        RestrictionResponse restriction = restrictionService.approveRestriction(partnerUserId, request);
        
        if (restriction == null) {
            ApiResponse<RestrictionResponse> response = ApiResponse.success(
                null,
                "Restriction rejected"
            );
            return ResponseEntity.ok(response);
        }
        
        ApiResponse<RestrictionResponse> response = ApiResponse.success(
            restriction,
            restriction.isActive() ? "Restriction activated successfully" : "Restriction approved, awaiting other partner"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get restriction by ID
     * GET /api/restrictions/{restrictionId}
     */
    @GetMapping("/{restrictionId}")
    public ResponseEntity<ApiResponse<RestrictionResponse>> getRestrictionById(
            @PathVariable String restrictionId) {
        
        log.info("GET /api/restrictions/{} - Fetching restriction", restrictionId);
        
        RestrictionResponse restriction = restrictionService.getRestrictionById(restrictionId);
        ApiResponse<RestrictionResponse> response = ApiResponse.success(
            restriction,
            "Restriction retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all restrictions for a couple
     * GET /api/restrictions/couple/{coupleId}
     */
    @GetMapping("/couple/{coupleId}")
    public ResponseEntity<ApiResponse<List<RestrictionResponse>>> getRestrictionsByCouple(
            @PathVariable String coupleId) {
        
        log.info("GET /api/restrictions/couple/{} - Fetching all restrictions", coupleId);
        
        List<RestrictionResponse> restrictions = restrictionService.getRestrictionsByCouple(coupleId);
        ApiResponse<List<RestrictionResponse>> response = ApiResponse.success(
            restrictions,
            String.format("%d restriction(s) retrieved", restrictions.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get active restrictions
     * GET /api/restrictions/couple/{coupleId}/active
     */
    @GetMapping("/couple/{coupleId}/active")
    public ResponseEntity<ApiResponse<List<RestrictionResponse>>> getActiveRestrictions(
            @PathVariable String coupleId) {
        
        log.info("GET /api/restrictions/couple/{}/active", coupleId);
        
        List<RestrictionResponse> restrictions = restrictionService.getActiveRestrictionsByCouple(coupleId);
        ApiResponse<List<RestrictionResponse>> response = ApiResponse.success(
            restrictions,
            String.format("%d active restriction(s)", restrictions.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending restrictions
     * GET /api/restrictions/couple/{coupleId}/pending
     */
    @GetMapping("/couple/{coupleId}/pending")
    public ResponseEntity<ApiResponse<List<RestrictionResponse>>> getPendingRestrictions(
            @PathVariable String coupleId) {
        
        log.info("GET /api/restrictions/couple/{}/pending", coupleId);
        
        List<RestrictionResponse> restrictions = restrictionService.getPendingRestrictionsByCouple(coupleId);
        ApiResponse<List<RestrictionResponse>> response = ApiResponse.success(
            restrictions,
            String.format("%d pending restriction(s)", restrictions.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get restrictions expiring soon
     * GET /api/restrictions/couple/{coupleId}/expiring-soon
     */
    @GetMapping("/couple/{coupleId}/expiring-soon")
    public ResponseEntity<ApiResponse<List<RestrictionResponse>>> getExpiringSoonRestrictions(
            @PathVariable String coupleId) {
        
        log.info("GET /api/restrictions/couple/{}/expiring-soon", coupleId);
        
        List<RestrictionResponse> restrictions = restrictionService.getExpiringSoonRestrictions(coupleId);
        ApiResponse<List<RestrictionResponse>> response = ApiResponse.success(
            restrictions,
            String.format("%d restriction(s) expiring within 7 days ⏰", restrictions.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get restrictions by status
     * GET /api/restrictions/couple/{coupleId}/status/{status}
     */
    @GetMapping("/couple/{coupleId}/status/{status}")
    public ResponseEntity<ApiResponse<List<RestrictionResponse>>> getRestrictionsByStatus(
            @PathVariable String coupleId,
            @PathVariable String status) {
        
        log.info("GET /api/restrictions/couple/{}/status/{}", coupleId, status);
        
        List<RestrictionResponse> restrictions = restrictionService.getRestrictionsByStatus(coupleId, status);
        ApiResponse<List<RestrictionResponse>> response = ApiResponse.success(
            restrictions,
            String.format("%d %s restriction(s)", restrictions.size(), status.toLowerCase())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get restrictions by severity
     * GET /api/restrictions/couple/{coupleId}/severity/{severity}
     */
    @GetMapping("/couple/{coupleId}/severity/{severity}")
    public ResponseEntity<ApiResponse<List<RestrictionResponse>>> getRestrictionsBySeverity(
            @PathVariable String coupleId,
            @PathVariable String severity) {
        
        log.info("GET /api/restrictions/couple/{}/severity/{}", coupleId, severity);
        
        List<RestrictionResponse> restrictions = restrictionService.getRestrictionsBySeverity(coupleId, severity);
        ApiResponse<List<RestrictionResponse>> response = ApiResponse.success(
            restrictions,
            String.format("%d %s restriction(s)", restrictions.size(), severity.toLowerCase())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update restriction (extend end date, modify)
     * PUT /api/restrictions/{restrictionId}
     */
    @PutMapping("/{restrictionId}")
    public ResponseEntity<ApiResponse<RestrictionResponse>> updateRestriction(
            @PathVariable String restrictionId,
            @Valid @RequestBody RestrictionUpdateRequestDto request) {
        
        log.info("PUT /api/restrictions/{} - Updating restriction", restrictionId);
        
        request.setRestrictionId(restrictionId);
        RestrictionResponse restriction = restrictionService.updateRestriction(request);
        ApiResponse<RestrictionResponse> response = ApiResponse.success(
            restriction,
            "Restriction updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Revoke restriction early
     * POST /api/restrictions/{restrictionId}/revoke
     */
    @PostMapping("/{restrictionId}/revoke")
    public ResponseEntity<ApiResponse<RestrictionResponse>> revokeRestriction(
            @PathVariable String restrictionId,
            @RequestParam String pin) {
        
        log.info("POST /api/restrictions/{}/revoke - Revoking restriction", restrictionId);
        
        RestrictionResponse restriction = restrictionService.revokeRestriction(restrictionId, pin);
        ApiResponse<RestrictionResponse> response = ApiResponse.success(
            restriction,
            "Restriction revoked successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete restriction
     * DELETE /api/restrictions/{restrictionId}
     */
    @DeleteMapping("/{restrictionId}")
    public ResponseEntity<ApiResponse<Void>> deleteRestriction(
            @PathVariable String restrictionId,
            @RequestParam String pin) {
        
        log.info("DELETE /api/restrictions/{} - Deleting restriction", restrictionId);
        
        restrictionService.deleteRestriction(restrictionId, pin);
        ApiResponse<Void> response = ApiResponse.success("Restriction deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Auto-expire overdue restrictions (admin/scheduler endpoint)
     * POST /api/restrictions/admin/expire-overdue
     */
    @PostMapping("/admin/expire-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> expireOverdueRestrictions() {
        
        log.info("POST /api/restrictions/admin/expire-overdue - Running auto-expiry");
        
        int expiredCount = restrictionService.expireOverdueRestrictions();
        Map<String, Object> result = Map.of(
            "expiredCount", expiredCount,
            "message", String.format("%d restriction(s) auto-expired", expiredCount)
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(
            result,
            "Auto-expiry completed"
        );
        
        return ResponseEntity.ok(response);
    }
}